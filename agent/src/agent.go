package main

import (
  "crypto/tls"
  "crypto/x509"
  "fmt"
  "io/ioutil"
  "net/http"
  // "strings"
  "log"
  "os"
  // "time"
  // "bytes"
)

var baseID string

func main() {
  // number of ground-control stations available
  numGCS := 5;

  if (len(os.Args) < 3) {
    incorrectExecFormat();
    panic("Exiting")
  }
  baseURL := os.Args[2];
  log.SetFlags(log.Lshortfile)

  // set up re-usable client (thread safe)
  client := setupClient(os.Args[1])

  // read in base ID
  baseIDpath := "/usr/share/id.dat"
  var err error
  baseID, err = ReadBaseID(baseIDpath)
  if err != nil {
    fmt.Println("ERROR! Unable to read base ID",baseIDpath)
    log.Fatal(err)
    panic("Exiting")
  }

  queueStatus := make(chan bool)
  fmt.Println("Launching Queue")
  go Queue(queueStatus)

  if (len(os.Args) == 4) {
    // probably testing
    if (os.Args[3] == "test") {
      RunTests(client,baseURL)
    } else {
        fmt.Println("Did not understand 4th argument");
        incorrectExecFormat();
        panic("Exiting")
    }
  } else {
        // regular run
        var GCSavailableURL string;
        GCSavailableURL = findAvailableGCS(client,baseURL,numGCS)
        fmt.Println(GCSavailableURL)
  }

  empty := false
  for ; !empty; {
    fmt.Println("Checking Queue Status");
    empty = <-queueStatus
    fmt.Println("Status:",empty);
  }

}

func readPEM(filePath string) ([]byte, error) {
  return ioutil.ReadFile(filePath)
}

func incorrectExecFormat() {
  fmt.Println("ERROR! Incorrect number of command line arguments!")
  fmt.Println("Command format:")
  fmt.Println("./agent path/to/client.PEM baseURL [test]")
  fmt.Println("Example")
  fmt.Println("./agent /etc/ssl/certs/server-cert.pem https://fruitfly-gcs:8000")
}

func setupClient(PEMfilePath string) *http.Client {
  rootPEM, err := readPEM(PEMfilePath)
  if err != nil {
    fmt.Println("ERROR! Unable to read root PEM",PEMfilePath)
    log.Fatal(err)
  }

  roots := x509.NewCertPool()
  ok := roots.AppendCertsFromPEM([]byte(rootPEM))
  if !ok {
    panic("failed to parse root certificate")
  }

  tr := &http.Transport{
    TLSClientConfig: &tls.Config{RootCAs: roots},
  }
  return &http.Client{Transport: tr}
}

func findAvailableGCS(client *http.Client, baseURL string,numGCS int) string {
  return "unimplemented"
}
