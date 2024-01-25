package main

// Implements a thread-safe, non-blocking queue that stores messages and sends
// whenever possible

import (
  "strings"
  "io/ioutil"
  "net/http"
  "bytes"
  "fmt"
  "errors"
  "encoding/json"
  "container/list"
)

// Element : element of list
// NOTE: This should probably be in its own package because of this definition
// type Element struct {
//   Value *fullMessage
// }

// Message : generic message struct sent from agents
type Message struct {
  Client    *http.Client
  BaseURL   string
  ID        string
  DataType  string
  Data      interface{}
}

// Location : special struct for location data (note, this is also how to define
// data that is always sent together)
type Location struct {
  Lat       float64
  Lng       float64
  Alt       float64
  Time      int64
}

// GenericData : single sensor/data, time-series structure
type GenericData struct {
  Dat       float64
  Time      int64
}

type fullMessage struct {
  client  *http.Client
  baseURL string
  t       telegram
}

type telegram struct {
  ID        string
  Number    int64
  DataType  string
  Data      interface{}
}

// FreeList : list of message channels to use for storing/queuing messages
// var FreeList = make(chan *Message, 100)

// MessageChannel : channel to use when sending message to queue
var MessageChannel = make(chan *Message)

// AddToQueue : call this function on a message to add it to the queue
// for sending
func AddToQueue(client *http.Client, baseURL, ID, DataType string, data interface{}) {
  // testChannel := make(chan *Message)
  fmt.Println("Adding to queue")
  if MessageChannel == nil {
    panic("MessageChannel is nil!!!")
  }
  // var m *Message
  // select {
  // case m = <-FreeList:
  //   fmt.Println("Using FreeList")
  //   // Got one...
  // default:
  //   fmt.Println("Using default")
  //   // None free, so allocate a new one.
  //   m = new(Message)
  // }
  m := &Message{client, baseURL, ID, DataType, data} // define message contents
  fmt.Println("Message defined; len")
  MessageChannel <- m // send message
  fmt.Println("Message sent")
}

// TODO: Better error handling
func store(t *telegram) {
  filePath := "log.txt"
  b, err := json.Marshal(*t)
  if err != nil {
      fmt.Println(err)
  }
  err = ioutil.WriteFile(filePath,b, 0664)
  if err != nil {
    fmt.Println(err)
  }
}

func addToList(l *list.List, m *Message, messageNum int64) {
  fmt.Println("Adding to list")
  maxQueuedMessages := 1000
  t := telegram{(*m).ID, messageNum, (*m).DataType, (*m).Data}
  if l.Len() > maxQueuedMessages {
  } else {
    l.PushBack(&fullMessage{(*m).Client, (*m).BaseURL, t})
  }
  // append *all* new messages to a messages.dat file
  store(&t);
}

// Queue : Thread that receives and queues messages before sending them
// architecture being implemented: https://golang.org/doc/effective_go.html#leaky_buffer
// sets emptyQueue to true when list is empty
func Queue(emptyQueue chan<- bool) {
  fmt.Println("Inside Queue")
  // FreeList = make(chan *Message, 100)
  // MessageChannel = make(chan *Message)
  // fmt.Println("Initialized channels")
  var messageNum int64
  firstMessage := true
  l := list.New()
  messageNum = 0;
  threadReturned := make(chan bool)

  fmt.Println("Launching infinite for loop")

  for {
    select {
    case m := <-MessageChannel:
      fmt.Println("Inside MessageChannel")
      // not just the default case, because it might be that there's telegrams
      // in the queue but also messages on the channel--> thus, randomly go between

      // add message to queue (list)
      // m := <-MessageChannel
      messageNum = messageNum + 1
      addToList(l, m, messageNum)
      // select {
      // case FreeList <- m:
      //   // Message on free list; nothing more to do.
      // default:
      //   // Free list full, just carry on
      // }
    default:
      // fmt.Println("Inside default")
      // block only if there are no messages to send
      if l.Len() == 0 {
        fmt.Println("Len was 0")
        // let main process know the queue is empty
        select {
        case emptyQueue <- true:
          // main process is wanting to know our status
        default:
          // main process doesn't care
        }

        fmt.Println("tried to write true to emptyQueue and now waiting for message")
        // block when there's nothing to be sent and no messages on the channel
        // wait for work
        m := <-MessageChannel
        // let the main process know there's at least one message
        select {
        case emptyQueue <- false:
          // main process is wanting to know our status
        default:
          // main process doesn't care
        }
        messageNum = messageNum + 1
        addToList(l, m, messageNum)
        // select {
        // case FreeList <- m:
        //   // Message on free list; nothing more to do.
        // default:
        //   // Free list full, just carry on
        // }
      }

    }

    select {
    case confirmed := <-threadReturned:
      fmt.Println("Inside thread return")
      fmt.Println("confirmed:",confirmed)
      if confirmed {
        fmt.Println("removing element",l.Len())
        // remove that telegram
        l.Remove(l.Front());
        fmt.Println("element removed",l.Len())
      }
      // launch thread to post the message, (for performance, possibly multiple threads;
      // but need to pass different argument)
      // however, don't launch more than a particular number
      if l.Len() != 0 {
        // only try to send something that's there
        e := l.Front()
        go postThread(e.Value.(*fullMessage), threadReturned)
      } else {
        firstMessage = true
      }
    default:
      // just loop and wait for thread to return
      if firstMessage {
        firstMessage = false
        e := l.Front()
        fmt.Printf("e %+v\n",(e.Value))
        go postThread(e.Value.(*fullMessage), threadReturned)
      }
    }
  }
}

func postThread(f *fullMessage, ret chan<- bool) {
  confirmed, _ := PostMessage((*f).client, (*f).baseURL, (*f).t)
  fmt.Println("PostMessage returned")
  ret <- confirmed
  fmt.Println("Confirmation written to ret")
}

// PostMessage : POST's a message to the webserver; returns true if confirmed, false otherwise
// TODO: Add proper error handling
func PostMessage(client *http.Client, baseURL string, t telegram) (bool, error) {
  link := baseURL + "/message"

  b, err := json.Marshal(t)
  if err != nil {
      fmt.Println(err)
      return false, err
  }

  resp, err := client.Post(link, "text/plain", bytes.NewReader(b))
  if err != nil {
    fmt.Println(err)
    return false, err
  }
  defer resp.Body.Close()
  content, err := ioutil.ReadAll(resp.Body)
  if err != nil {
    return false, err
  }

  confirmation := strings.TrimSpace(string(content))
  fmt.Println(confirmation)
  if (confirmation == "confirmed") {
    fmt.Println("returning true")
    return true, nil
  }
  return false, errors.New("not confirmed")
}
