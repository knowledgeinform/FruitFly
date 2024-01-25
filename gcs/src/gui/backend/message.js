const register = require('./register.js');
const fs = require('fs');
const EventEmitter = require('events');


class MessageEmitter extends EventEmitter {}

const mEmitter = new MessageEmitter();

function store(body) {
  // logBody = body.concat(";");
  var logFilePath = "log.txt";
  var options = {encoding: 'utf8', flag: 'a'};
  fs.writeFileSync(logFilePath, body, options);
}

function incrementMessage(obj) {
  var mNumber = register.clientMap.get(obj.ID).messageNumber;
  register.clientMap.get(obj.ID).messageNumber = mNumber + 1;
  register.clientMap.get(obj.ID).messageStored = false;
}

module.exports = {
  message: function (request, response) {
    if (request.method === 'POST') {
      // check for registration or just pass through?
      // for now, just pass through
      console.log('In POST');
      let body = [];
      request.on('data', (chunk) => {
        body.push(chunk);
      }).on('end', () => {
        // unmarshal
        body = Buffer.concat(body).toString();
        m = JSON.parse(body);
        console.log(body);
        // check registration
        if (register.clientMap.has(m.ID) == false) {
          reponse.end("registration required");
        } else {
          if (m.Number < register.clientMap.get(m.ID).messageNumber) {
            console.log("Already received message");
            // send back confirmation to increment to next message since this one
            // was already stored and sent on
            response.end("confirmed");
          } else {
            console.log("Incrementing message");
            incrementMessage(m);
            store(body);
            register.clientMap.get(m.ID).messageStored = true;
            mEmitter.emit('received', m);
          }
          response.end("confirmed");
        }
      });

    } else {
      console.log('404');
      response.statusCode = 404;
      response.end();

    }
  },
  messageEmitter: mEmitter,
  store: store
};
