const gcsmap = require('./gcsmap.js');

function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

function displayBoxAndMessage(document, m) {
  // remove old alert if it exists
  var oldAlert = document.getElementById("alert-div");
  if (oldAlert != null) {
    oldAlert.parentNode.removeChild(oldAlert);
  }

  var alertBox = document.createElement("div");
  alertBox.className = "alertClass";
  alertBox.id = "alert-div";
  var alertTitle = document.createTextNode("Message Alert:\n");
  alertBox.appendChild(alertTitle);
  // console.log(m.Data);
  var alertMessage = document.createTextNode(m.Data.Dat);
  alertBox.appendChild(alertMessage);

  var insertElem = document.getElementById(gcsmap.map.tlID);
  insertAfter(alertBox,insertElem);
}

module.exports = {
  display: function(document, m) {
    displayBoxAndMessage(document, m);
  }
};
