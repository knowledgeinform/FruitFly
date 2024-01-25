package main

import (
  // "crypto/tls"
  // "crypto/x509"
  "fmt"
  "io/ioutil"
  "net/http"
  "strings"
  // "log"
  // "os"
  "math"
  "time"
  "math/rand"
  "bytes"
)

func echoTest(client *http.Client, baseURL string) {
  link := baseURL + "/echo"
  response, err := client.Get(link)
  if err != nil {
      fmt.Println(err)
  }
  defer response.Body.Close()

  content, _ := ioutil.ReadAll(response.Body)
  s := strings.TrimSpace(string(content))

  fmt.Println(s)

  out := s + " world"

  resp, err := client.Post(link, "text/plain", bytes.NewReader([]byte(out)))
  if err != nil {
    fmt.Println(err)
  }
  defer resp.Body.Close()
  content, _ = ioutil.ReadAll(resp.Body)
  s = strings.TrimSpace(string(content))

  fmt.Println(s)
}

func registerTest(client *http.Client, baseURL string) {
  link := baseURL + "/register"
  response, err := client.Get(link)
  if err != nil {
      fmt.Println(err)
  }
  defer response.Body.Close()

  content, _ := ioutil.ReadAll(response.Body)
  s := strings.TrimSpace(string(content))

  fmt.Println(s)

  RegisterWithServer(client, baseURL);

}

func messageTest(client *http.Client, baseURL string) {
  rand.Seed(time.Now().UnixNano());
  latRange := [2]float64{39.453383, 39.480555};
  lngRange := [2]float64{-76.208175, -76.134511};

  randPercent := rand.Float64();
  lat := (1.0 - randPercent)*(latRange[1] - latRange[0]) + latRange[0];
  lng := (1.0 - randPercent)*(lngRange[1] - lngRange[0]) + lngRange[0];
  lat0 := lat
  lng0 := lng
  alt := 200.0

  // PostMessage(client,baseURL,m)
  loc := Location{lat, lng, alt, (time.Now().UnixNano() / int64(time.Millisecond))}
  AddToQueue(client, baseURL, baseID, "Location", &loc)

  var genConc GenericData

  for i := 1; i < 500; i++ {
    time.Sleep(200 * time.Millisecond)

    lat = lat0 + float64(i)*math.Cos(float64(i)/10.0)/50000
    lng = lng0 + float64(i)*math.Sin(float64(i)/10.0)/10000
    alt = 100.0*math.Cos(float64(i)/10.0) + 200.0
    loc = Location{lat, lng, alt, (time.Now().UnixNano() / int64(time.Millisecond))}
    AddToQueue(client, baseURL, baseID, "Location", &loc)
    genConc = GenericData{rand.Float64(), (time.Now().UnixNano() / int64(time.Millisecond))}
    AddToQueue(client, baseURL, baseID, "Concentration",&genConc)
  }
}

// RunTests : function that runs a series of client-server test interactions
// TODO: Make these tests have checks
func RunTests(client *http.Client, baseURL string) {
  fmt.Println("TEST: Running echo test")
  echoTest(client,baseURL)
  fmt.Println("TEST: Running register test")
  registerTest(client,baseURL)
  fmt.Println("TEST: Running message test")
  messageTest(client,baseURL)
}
