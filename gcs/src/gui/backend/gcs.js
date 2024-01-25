const https = require('https');
const fs = require('fs');
const http = require('http');
const net = require('net');
const path = require('path');
const echo = require('./echo.js');
const register = require('./register.js');
const message = require('./message.js');

const port = 8000;
const internalPort = 3000;

// var keyloc = process.argv[2];
// var certloc = process.argv[3];
// var caloc = process.argv[4];
var keyloc = "server-key.pem";
var certloc = "server-cert.pem";
var caloc = "server-cert.pem";

var options;

function requestHandler (request, response) {
  if (request.url === '/echo') {
    echo.echo(request,response);
  } else if (request.url === '/register') {
    // register new client
    register.register(request, response);
  } else if (request.url === '/message') {
    // receive client's message
    message.message(request, response);
  } else {
    console.log('404');
    response.statusCode = 404;
    response.end();
  }
}

let agentServer;
// var client;

module.exports = {
  start: function() { agentServer = https.createServer(options, (request, response) => {
      // TODO: use request.authorized in if statement to allow/disallow authorized/unauthorized
      // connections. For now, just note it.
      console.log('server connected', request.authorized ? 'authorized' : 'unauthorized');
      request.on('error', (err) => {
        console.error(err);
        response.statusCode = 400;
        response.end();
      });
      response.on('error', (err) => {
        console.error(err);
      });
      requestHandler(request, response);
      console.log('done');
    }).listen(port);
  },
  stop: function() {
    if (agentServer !== undefined) {
      agentServer.close();
    }
    if (client !== undefined) {
      client.destroy();
      client.unref();
    }
  },
  ID: function() {
    return register.ID();
  },
  loc: function() {
    // console.log("gcs loc");
    return register.loc();
  },
  initializeOptions: function(basePath, user_keyloc, user_certloc, user_caloc) {
    var keyPath, certPath, caPath;
    if (user_keyloc === undefined) {
      keyPath = path.join(basePath, keyloc);
    } else {
      keyPath = path.join(basePath, user_keyloc);
    }

    if (user_certloc === undefined) {
      certPath = path.join(basePath, certloc);
    } else {
      certPath = path.join(basePath, user_certloc);
    }

    if (user_caloc === undefined) {
      caPath = path.join(basePath, caloc);
    } else {
      caPath = path.join(basePath, user_caloc);
    }

    options  = {
      key: fs.readFileSync(keyPath),
      cert: fs.readFileSync(certPath),
      rejectUnauthorized: true,
      // This is necessary only if using client certificate authentication.
      // requestCert: true,

      // This is necessary only if the client uses a self-signed certificate.
      // TODO: Make an actual CA and use this to sign the certificates
      ca: [ fs.readFileSync(caPath) ]

    };
  }
};
