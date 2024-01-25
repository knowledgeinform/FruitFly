#!/bin/sh
if [ -z "$1" ]; then
  echo "You must enter the FQDN of the client"
  exit
else
  echo "Certificate name: ${1}-cert.pem"
fi

openssl genrsa -out "${1}-key.pem" 4096
openssl req -new -key "${1}-key.pem" -out "${1}-csr.pem"
openssl x509 -req -in "${1}-csr.pem" -signkey "${1}-key.pem" -out "${1}-cert.pem"
