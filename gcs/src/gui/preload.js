const gcsmap = require('./gcsmap.js');
const overlay = require('./overlay.js');
const gcs = require('./backend/gcs.js');
const { ipcRenderer, webFrame } = require('electron');
const register = require('./backend/register.js');
const pl = require('./plotter');
const dataManager = require('./data.js');
const breadcrumbs = require('./breadcrumbs.js');
const moveIcon = require('./moveIcon.js');
const { remote } = require('electron');
const { Menu, MenuItem } = remote;
const customMenu = require('./customMenu.js');
const messageBox = require('./messageBox.js');
const path = require('path');

var map;

// ipcRenderer.on('basePath', (e, bp) => {
//   console.log("registering base path");
//   register.initializeBasePath(bp);
// });

// All of the Node.js APIs are available in the preload process.
// It has the same sandbox as a Chrome extension.
window.addEventListener('DOMContentLoaded', () => {
  var bp = ipcRenderer.sendSync('reqBasePath');
  // console.log("Got reply: "+bp);
  register.initializeBasePath(bp);
  gcsmap.initializeBasePath(bp);

  const replaceText = (selector, text) => {
    const element = document.getElementById(selector);
    if (element) element.innerText = text;
  };

  for (const type of ['chrome', 'node', 'electron']) {
    replaceText(`${type}-version`, process.versions[type]);
  }
  map = gcsmap.mapInit(document);
  dataManager.initialize();
  // console.log("Adding agent1 to map");
  // dataManager.addToDataMap('agent1');
  // console.log("Added agent1 to map");

  console.log(window.performance.memory.jsHeapSizeLimit);
});

window.addEventListener('load', function() {
  console.log("loading event listeners");
  if (map === undefined) {
    console.log("MAP NOT DEFINED YET!!!");
  } else {
    console.log("preload overlay home");
    console.log(map);
    overlay.home(gcs.loc(), document, map, gcsmap.findTile(gcs.loc()));
    pl.start(dataManager.data, document);
    console.log("Zoom level");
    console.log(webFrame.getZoomFactor());

    window.addEventListener("resize", function() {
      var elem = document.getElementById(gcsmap.map.tlID);
      gcsmap.map.tOffset = elem.offsetTop;
      gcsmap.map.lOffset = elem.offsetLeft;
      gcsmap.resizeMap(document);
      overlay.home(gcs.loc(), document, gcsmap.map, gcsmap.findTile(gcs.loc()));
      moveIcon.reposition(document);
    });
    // overlay.initializeIconListeners(document);
  }
});

function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

function addIcon(clientObj, type, document) {
  // console.log(document);
  var svg = document.getElementById(type);
  // duplicate svg and set to visible
  // this is easier said than done: the cloned node doesn't have the rendered document
  // with all the svg paths attached. Thus, the implementation is: 1. clone the node
  // 2. make the cloned node essentially the original node 3. Change the name of the original
  // node 4. change the properties of the original node, which actually become the
  // new node seen on the screen
  var svg2 = svg.cloneNode(true);
  // var svg2doc = svg.contentDocument.cloneNode(true);
  svg.id = clientObj.self.concat("-icon");
  svg2.id = type;
  svg.parentNode.insertBefore(svg2, svg);
  svg.style.visibility = "visible";
  svg.style.color = clientObj.color;
  // svg.style.fill = clientObj.color;
  // console.log(svg);
  var elements = svg.contentDocument.getElementsByTagName("path");
  // console.log(elements[0]);
  // console.log(clientObj);
  for (var element of elements) {
    element.style.fill = clientObj.color;
  }
}

ipcRenderer.on('overlayClient', (e, clientObj) => {
  console.log("inside overlay client event");

  if (clientObj.self.slice(0,5).localeCompare("agent") == 0) {
    dataManager.addToDataMap(clientObj.self);
    console.log("Added to data map");
    addIcon(clientObj, 'agent-icon', document);
    pl.updateOptions([clientObj.self],document,"agent-selector");

  } else if (clientObj.self.slice(0,3).localeCompare("gcs") == 0) {
    addIcon(clientObj, 'gcs-icon', document);
  } else {
    alert("UNKNOWN CLIENT: "+clientObj.self);
  }
});

ipcRenderer.on('overlayMessage', (e, m) => {
  // console.log("inside overlay message event");
  // console.log(m);
  if (m.ID.slice(0,5).localeCompare("agent") === 0) {
    // console.log(m);
    dataManager.addDataPoint(m.ID, m.DataType, m.Data);
    if (m.DataType.localeCompare("UNKNOWN") == 0) {
      // display debug/alert message
      console.log("Preload: unknown message found");
      messageBox.display(document, m);

    } else if (m.DataType != undefined) {
      if (m.DataType.localeCompare("Location") == 0) {
        moveIcon.agent(m, document);
      }
      breadcrumbs.dropBC(m, document);
      // console.log(dataManager.data);
      pl.update(m, dataManager.data);
    }


  } else if (m.ID.slice(0,3).localeCompare("gcs") === 0) {
    moveIcon.gcs(m, document);
  } else {
    alert("UNKNOWN CLIENT: "+clientObj.self);
  }
});

const menu = new Menu();
menu.append(new MenuItem({ label: 'Set Minimum Concentration', click() {
  customMenu.setMinimumConcentration(document);
} }));
menu.append(new MenuItem({ label: 'Set Maximum Concentration', click() {
  customMenu.setMaximumConcentration(document);
} }));
menu.append(new MenuItem({ label: 'Set Shrink Time', click() {
  customMenu.setShrinkTime(document);
} }));

window.addEventListener('contextmenu', (e) => {
  e.preventDefault();
  menu.popup({ window: remote.getCurrentWindow() });
}, false);
