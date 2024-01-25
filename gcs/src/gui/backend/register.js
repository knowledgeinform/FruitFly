const fs = require('fs');
const EventEmitter = require('events');
const path = require('path');


var basePath;
var client = new Map();

function gcsID() {
  // console.log("gcsID: "+basePath);
  var idFilePath = path.join(basePath, "id.dat");
  var id;
  id = undefined;
  try {
    id = fs.readFileSync(idFilePath, 'utf8');
  } catch(e) {
    console.log('Error:', e.stack);
  }
  return id;
}

function gcsLoc() {
  var locFilePath = path.join(basePath,"loc.dat");
  var loc;
  loc = undefined;
  // console.log("Reading location");
  try {
    loc = JSON.parse(fs.readFileSync(locFilePath, 'utf8'));
  } catch(e) {
    console.log('Error:', e.stack);
  }
  // console.log(loc);
  return loc;
}

class RegEmitter extends EventEmitter {}

const rEmitter = new RegEmitter();

function padBinary(numItems) {
  var binaryStr = (numItems & 7).toString(2);
  if ((numItems & 7) <= 3) {
    if ((numItems & 7) <= 1) {
      // pad with two zeros
      binaryStr = "00".concat(binaryStr);
    } else {
      // pad with one zero
      binaryStr = "0".concat(binaryStr);
    }
  }
  // console.log("padBinary");
  // console.log(binaryStr);
  return binaryStr;
}

function str2Array(str) {
  var array = [str.length];
  for (var i = 0; i < str.length; i++) {
    array[i] = parseInt(str.charAt(i));
  }
  // console.log("str2array");
  // console.log(array);
  return array;
}

// assumes array is solely 1s and 0s
function scaleArray(array, length) {
  for (var i = 0; i < array.length; i++) {
    array[i] = array[i]*length;
  }
  // console.log("scaleArray");
  // console.log(array);
  return array;
}

function pow8scale(array, numItems) {
  // bisects (currently, just goes upward)
  // TODO: alternate sides of bisection
  var pow;
  if (numItems < 8) {
    pow = 0;
  } else {
    pow = Math.trunc((numItems - 8)/8) + 1;
  }
  var divisor = Math.pow(2, pow);

  // this could be optimized better, but it does the job for now
  for (var i = 0; i < array.length; i ++) {
    array[i] = Math.trunc(array[i] / divisor);
  }
  // console.log("pow8scale");
  // console.log(pow);
  // console.log(array);
  return array;
}

// incrementing number that skips (num & 7) == 0 cases
var uniqueNum = 0;

// generates unique color code for each new client
function setColor(numItems) {
  uniqueNum = uniqueNum + 1;
  if ((uniqueNum & 7) == 0) {
    uniqueNum = uniqueNum + 1;
  }
  var uniqueStr = padBinary(uniqueNum);
  var threeDArray = str2Array(uniqueStr);
  threeDArray = scaleArray(threeDArray,255);
  threeDArray = pow8scale(threeDArray, uniqueNum);
  // console.log("threeDArray");
  // for (var a of threeDArray) {
  //   console.log(a);
  // }
  return 'rgba('.concat(threeDArray[0].toString(),',',threeDArray[1].toString(),',',threeDArray[2].toString(),')');
}

function addToMap(baseID) {
  // base ID could be full id if previously registered
  if (client.has(baseID)) {
    return baseID;
  } else {
    var newMapSize = client.size + 1;
    var stringID = baseID.concat(newMapSize.toString());
    // the client.set command can be expanded to link other variables, functions,
    // etc. with the stringID. For now, just the string "registered" is linked
    // with the id
    client.set(stringID,
      {'registered' : true,
      'messageNumber' : 0,
      'messageStored' : false,
      'color' : setColor(client.size),
      'self' : stringID});
    console.log("emitting event");
    console.log(client.get(stringID));
    rEmitter.emit('registered', client.get(stringID), stringID);
    console.log("Event emitted");
    return stringID;
  }
}

module.exports = {
  clientMap: client,
  register: function (request, response) {
    if (request.method === 'GET') {
      // send gcs's id
      console.log('In GET');
      var id = gcsID();
      if (id === undefined) {
        response.statusCode = 500; // internal server error
        response.end();
      } else {
        // console.log(id);
        response.end(id);
      }

    } else if (request.method === 'POST') {
      console.log('In POST');
      let body = [];
      var baseID, uniqueNumber;
      request.on('data', (chunk) => {
        body.push(chunk);
      }).on('end', () => {
        baseID = Buffer.concat(body).toString();
        console.log(baseID);
        uniqueID = addToMap(baseID);
        console.log(uniqueID);
        response.end(uniqueID);
      });

    } else {
      console.log('404');
      response.statusCode = 404;
      response.end();

    }
  },
  ID: function() {
    return gcsID();
  },
  loc: function() {
    // console.log("register loc");
    return gcsLoc();
  },
  regEmitter: rEmitter,
  setColor: setColor,
  initializeBasePath: function(bp) {
    console.log("register: inializing basepath: "+bp);
    basePath = bp;
  }
};
