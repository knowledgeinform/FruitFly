const gcsmap = require('./gcsmap.js');
const overlay = require('./overlay.js');

function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

var breadCrumbMap = new Map();

function initializeBCs(obj, document, id) {
  var type = "bread-crumb-icon";
  var el = document.getElementById(type);
  for (var i = 0; i < obj.length; i++) {
    obj.map.set(i,{update: false, loc: undefined, conc: undefined, last: undefined});
    var el2 = el.cloneNode(true);
    el2.id = id.concat("-bc-icon",i.toString());
    el2.style.borderColor = document.getElementById(id.concat("-icon")).style.color;
    insertAfter(el2,el);
  }

}

function addLocation2BC(m, obj) {
  // console.log("Adding Location");
  // console.log(obj.map);
  obj.map.get(obj.index).loc = {lat: m.Data.Lat, lng: m.Data.Lng};
  if (obj.map.get(obj.index).conc != undefined) {
    // console.log("Loc Setting update to true");
    obj.map.get(obj.index).update = true;
  }
}

function addConcentration2BC(m, obj) {
  // console.log("Adding concentration");
  // console.log(m);
  obj.map.get(obj.index).conc = m.Data.Dat;
  if (obj.map.get(obj.index).loc != undefined) {
    // console.log("Conc Setting update to true");
    obj.map.get(obj.index).update = true;
  }
}

function calculateColor(conc) {
  var minConc = document.getElementById("minConcField").value;
  var maxConc = document.getElementById("maxConcField").value;
  // console.log("min conc: "+minConc);
  // console.log("max conc: "+maxConc);
  colorMap = (255 / (maxConc - minConc)) * (conc - minConc);
  red = 0;
  green = colorMap;
  blue = 255;
  color = "rgba(".concat(red.toString(),",",green.toString(),",",blue.toString(),",0.5)");
  return color;
}

function updateBC(id, obj, document) {
  var shrinkTime = document.getElementById("shrinkTimeField").value;
  // console.log("Updating BC");
  // console.log(obj.array.get(obj.index));
  // console.log(id.concat("-bc-icon",obj.index.toString()));
  var el = document.getElementById(id.concat("-bc-icon",obj.index.toString()));

  el.style.left = Math.round(overlay.longitude2Pixel(gcsmap.map,obj.map.get(obj.index).loc)).toString().concat("px");
  el.style.top = Math.round(overlay.latitude2Pixel(gcsmap.map,obj.map.get(obj.index).loc)).toString().concat("px");
  obj.map.get(obj.index).last = {loc: {lat: obj.map.get(obj.index).loc.lat, lng: obj.map.get(obj.index).loc.lng}, conc: obj.map.get(obj.index).conc};

  var elements = el.contentDocument.getElementsByTagName("path");
  // console.log(elements[0]);
  // console.log(clientObj);
  for (var element of elements) {
    element.style.fill = calculateColor(obj.map.get(obj.index).conc);
  }
  el.style.visibility = 'visible';
  el.style.animation = 'bread-crumb-loc-animation '.concat(shrinkTime.toString(),'s');
  setTimeout(function() {
    el.style.animation = '';
  }, shrinkTime*1000);
  // el.addClass('fade-out');
  // setTimeout(function() {
  //   el.removeClass('fade-out');
  // }, 2000);
  // el.style.WebkitTransition = 'opacity 2s linear';
  // el.style.transition = 'opacity 2s linear';
  // el.style.opacity = 0;

  obj.map.get(obj.index).update = false;
  obj.map.get(obj.index).loc = undefined;
  obj.map.get(obj.index).conc = undefined;
  obj.index = (obj.index + 1) % obj.length;

}

function dropBreadCrumb(m, document) {
  // console.log("Inside drop bread crumb");
  maxNumBreadCrumbs = 100;
  if (!breadCrumbMap.has(m.ID)) {
    console.log("Initializing breadcrumb array");
    breadCrumbMap.set(m.ID, {map: new Map(), length: maxNumBreadCrumbs, index: 0});
    console.log(breadCrumbMap.get(m.ID));
    initializeBCs(breadCrumbMap.get(m.ID), document, m.ID);
  }

  if (m.DataType.localeCompare("Location") == 0) {
    addLocation2BC(m, breadCrumbMap.get(m.ID));
  } else if (m.DataType.localeCompare("Concentration") == 0) {
    // console.log(breadCrumbMap.get(m.ID));
    addConcentration2BC(m, breadCrumbMap.get(m.ID));
  } else {
    // neither of the important data types
  }

  if (breadCrumbMap.get(m.ID).map.get(breadCrumbMap.get(m.ID).index).update == true) {
    updateBC(m.ID, breadCrumbMap.get(m.ID), document);
  }

  // if (breadCrumbMap.get(m.ID).length > maxNumBreadCrumbs) {
  //   breadCrumbMap.get(m.ID).shift();
  // }

  // assign
}

function repositionAll(document, agentID) {
  if (!breadCrumbMap.has(agentID)) {
    console.log("No breadcrumbs for "+agentID);
  } else {
    obj = breadCrumbMap.get(agentID);
    for (var i = 0; i < obj.length; i++) {
      if (obj.map.get(i).last != undefined) {
        var el = document.getElementById(agentID.concat("-bc-icon",i.toString()));
        var vis = el.style.visibility;
        // console.log("index: "+i+" visibility: "+vis);
        // console.log(obj.map.get(i).last.loc.lat);
        // console.log(obj.map.get(i).last.loc.lng);
        el.style.left = Math.round(overlay.longitude2Pixel(gcsmap.map,obj.map.get(i).last.loc)).toString().concat("px");
        el.style.top = Math.round(overlay.latitude2Pixel(gcsmap.map,obj.map.get(i).last.loc)).toString().concat("px");
        el.style.visibility = vis;
      }
    }
  }

}

module.exports = {
  dropBC: function(m, document) {
    dropBreadCrumb(m, document);
  },
  reposition: function(document, agentID) {
    repositionAll(document, agentID);
  }
};
