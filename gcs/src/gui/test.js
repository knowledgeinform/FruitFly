var fs = require('fs');
var dataManager = require('./data.js');
// var Complex = require('complex.js');
//
// console.log(Complex("4+3i"));

var logData = fs.readFileSync('./log.txt', {encoding: 'utf8'});

// logData = Buffer.concat(logData);
// regex for one or more semicolons
var rawLogs = logData.split(/;+/);

var i = 0;

console.log("rawLogs length");
console.log(rawLogs.length);
console.log("i "+i);

async function sendRawMessage() {
  // return new Promise(resolve => {
  try {
    dataManager.ParseData(rawLogs[i]);
  } catch (err) {
    console.log("Error in sendRawMessage");
    console.log(err);
  }

  i = i + 1;
  console.log(rawLogs.length);
  console.log("i "+i);
  if (i < rawLogs.length) {
    setTimeout(function () {
      sendRawMessage();
    }, 400);
  }
  // });
}

// sendRawMessage();
module.exports = {
  fireData: sendRawMessage
};
