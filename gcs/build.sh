#!/bin/sh

CERTNAME=server-cert.pem
KEYNAME=server-key.pem

# copy key and cert for server side
echo "Copying over latest certifcate ${CERTNAME} and key ${KEYNAME}"
cp ../tls/$CERTNAME src/gui/backend
cp ../tls/$KEYNAME src/gui/backend

cd ../agent && ./build.sh && cd ../gcs
docker-compose up --build
