package main

import (
  "strings"
  "io/ioutil"
  "net/http"
  "bytes"
  "fmt"
)

// ReadBaseID : Reads the baseID from the id.dat file located in /usr/share/id.dat
func ReadBaseID(filePath string) (string, error) {
  b, err := ioutil.ReadFile(filePath)
  if err != nil {
    return "", err
  }
  s := string(b)
  return strings.TrimSuffix(s, "\n"), err
}

// RegisterWithServer : Registers agent with server, obtaining a unique number
// identifier
// TODO: Add proper error handling
func RegisterWithServer(client *http.Client, baseURL string) {
  link := baseURL + "/register"

  resp, err := client.Post(link, "text/plain", bytes.NewReader([]byte(baseID)))
  if err != nil {
    fmt.Println(err)
  }
  defer resp.Body.Close()
  content, _ := ioutil.ReadAll(resp.Body)
  baseID = strings.TrimSpace(string(content))

  fmt.Println(baseID)

  //TODO: Add proper caching feature for unique ID
  cacheID(baseID);
}

func cacheID(baseID string) {

  return
}
