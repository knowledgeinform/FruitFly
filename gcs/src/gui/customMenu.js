const breadcrumbs = require('./breadcrumbs.js');
const gcsmap = require('./gcsmap.js');

function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

var enterBtn;
var cancelBtn;

function keyUpFunction(e) {
  console.log("keyup detected "+e.keyCode);
  // 27 = ESC key
  console.log(this);
  if (e.keyCode === 27) {
    e.preventDefault();
    console.log("Canceling form");
    cancelBtn.click();
  }
}

function keyPressFunction(e) {
  console.log("keypress detected "+e.keyCode);
  if (e.keyCode === 13) {
    e.preventDefault();
    // what to do on submission
    console.log("Submitting form");
    enterBtn.click();
  }
}

function submitDefPrevent(e) {
  e.preventDefault();
}

function setMinConc(document) {
  var field = document.getElementById("minConcField");

  var minForm = document.getElementById("minimumConcentrationForm");
  minForm.addEventListener('submit', function(e) {
    e.preventDefault();
    // minForm.removeEventListener('submit', this);
  });

  cancelBtn = document.getElementById("minConcCancelBtn");
  cancelBtn.addEventListener("click", function(e) {
    // close form and don't do anything
    e.preventDefault();
    // console.log(arguments.callee);
    minForm.style.visibility = "hidden";
    field.removeEventListener("keypress", keyPressFunction);
    field.removeEventListener("keyup", keyUpFunction);
    minForm.removeEventListener('submit', submitDefPrevent);
    cancelBtn.removeEventListener("click",arguments.callee);
  });

  enterBtn = document.getElementById("minConcEnterBtn");
  enterBtn.addEventListener("click", function(e) {
    // store value somewhere, close form, and emit event
    e.preventDefault();
    if (parseFloat(field.value) >= parseFloat(document.getElementById("maxConcField").value)) {
      document.getElementById("maxConcField").value = parseFloat(field.value) + 1;
    }
    minForm.style.visibility = "hidden";
    field.removeEventListener("keypress", keyPressFunction);
    field.removeEventListener("keyup", keyUpFunction);
    minForm.removeEventListener('submit', submitDefPrevent);
    enterBtn.removeEventListener("click",arguments.callee);
  });

  field.addEventListener("keypress", keyPressFunction);
  field.addEventListener("keyup", keyUpFunction);

  minForm.style.visibility = "visible";
  field.focus();
  field.select();
}

function setMaxConc(document) {
  var field = document.getElementById("maxConcField");

  var maxForm = document.getElementById("maximumConcentrationForm");
  maxForm.addEventListener('submit', function(e) {
    e.preventDefault();
  });

  cancelBtn = document.getElementById("maxConcCancelBtn");
  cancelBtn.addEventListener("click", function(e) {
    // close form and don't do anything
    e.preventDefault();
    maxForm.style.visibility = "hidden";
    field.removeEventListener("keypress", keyPressFunction);
    field.removeEventListener("keyup", keyUpFunction);
    maxForm.removeEventListener('submit', submitDefPrevent);
    cancelBtn.removeEventListener("click",arguments.callee);
  });

  enterBtn = document.getElementById("maxConcEnterBtn");
  enterBtn.addEventListener("click", function(e) {
    // store value somewhere, close form, and emit event
    e.preventDefault();
    if (parseFloat(field.value) <= parseFloat(document.getElementById("minConcField").value)) {
      document.getElementById("minConcField").value = parseFloat(field.value) - 1;
    }
    maxForm.style.visibility = "hidden";
    field.removeEventListener("keypress", keyPressFunction);
    field.removeEventListener("keyup", keyUpFunction);
    maxForm.removeEventListener('submit', submitDefPrevent);
    enterBtn.removeEventListener("click",arguments.callee);
  });

  field.addEventListener("keypress", keyPressFunction);
  field.addEventListener("keyup", keyUpFunction);

  maxForm.style.visibility = "visible";
  field.focus();
  field.select();
}

function setShrinkTime(document) {
  var field = document.getElementById("shrinkTimeField");

  var shrinkForm = document.getElementById("shrinkTimeForm");
  shrinkForm.addEventListener('submit', submitDefPrevent);

  cancelBtn = document.getElementById("shrinkTimeCancelBtn");
  cancelBtn.addEventListener("click", function(e) {
    // close form and don't do anything
    e.preventDefault();
    shrinkForm.style.visibility = "hidden";
    field.removeEventListener("keypress", keyPressFunction);
    field.removeEventListener("keyup", keyUpFunction);
    shrinkForm.removeEventListener('submit', submitDefPrevent);
    cancelBtn.removeEventListener("click",arguments.callee);
  });

  enterBtn = document.getElementById("shrinkTimeEnterBtn");
  enterBtn.addEventListener("click", function(e) {
    // store value somewhere, close form, and emit event
    e.preventDefault();
    shrinkForm.style.visibility = "hidden";
    field.removeEventListener("keypress", keyPressFunction);
    field.removeEventListener("keyup", keyUpFunction);
    shrinkForm.removeEventListener('submit', submitDefPrevent);
    enterBtn.removeEventListener("click",arguments.callee);
  });

  field.addEventListener("keypress", keyPressFunction);
  field.addEventListener("keyup", keyUpFunction);

  shrinkForm.style.visibility = "visible";
  field.focus();
  field.select();
}

module.exports = {
  setMinimumConcentration: function(document) {
    setMinConc(document);
  },
  setMaximumConcentration: function(document) {
    setMaxConc(document);
  },
  setShrinkTime: function(document) {
    setShrinkTime(document);
  }
};
