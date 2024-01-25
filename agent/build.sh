#!/bin/sh

CERTNAME=server-cert.pem
KEYNAME=server-key.pem

# copy key and cert for server side
echo "Copying over latest certifcate ${CERTNAME} and key ${KEYNAME}"
cp ../tls/$CERTNAME src/
cp ../tls/$KEYNAME src/

cd src && env GOOS=linux GOARCH=arm GOARM=5 go build -o agent
