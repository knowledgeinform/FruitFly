FROM raspbian/stretch
WORKDIR /usr/local/bin
COPY src/agent .
COPY src/id.dat /usr/share/
COPY src/server-cert.pem /etc/ssl/certs/
COPY src/server-key.pem /etc/ssl/certs/
CMD ["./agent","/etc/ssl/certs/server-cert.pem","https://fruitfly-gcs:8000","test"]
