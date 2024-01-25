const gcsmap = require('./gcsmap.js');
const overlay = require('./overlay.js');
const breadcrumbs = require('./breadcrumbs.js');

// for orientation
var lastLoc = new Map();

function calculateAngle(newLoc, oldLoc) {
  return ( Math.atan2((newLoc.lng - oldLoc.lng), (newLoc.lat - oldLoc.lat)) *
    (180.0 / Math.PI) );
}

function moveAgentIcon(m, document) {
  var svg = document.getElementById(m.ID.concat("-icon"));
  var loc = {"lat": m.Data.Lat, "lng": m.Data.Lng};
  var angle;
  if (map === undefined) {
    console.log("MAP NOT DEFINED YET!!!");
  } else {
    if (svg.style.position === undefined) {
      svg.style.position = "absolute";
    } else {
      if (svg.style.position.localeCompare("absolute") != 0) {
        svg.style.position = "absolute";
      }
    }
    // console.log(map);
    l = Math.round(overlay.longitude2Pixel(gcsmap.map,loc)).toString().concat("px");
    t = Math.round(overlay.latitude2Pixel(gcsmap.map,loc)).toString().concat("px");
    // console.log(l);
    // console.log(t);
    svg.style.left = l;
    svg.style.top = t;
  }
  return loc;
}

function rotateAgentIcon(m, document) {
  var svg = document.getElementById(m.ID.concat("-icon"));
  var loc = {"lat": m.Data.Lat, "lng": m.Data.Lng};
  var angle;
  if (map === undefined) {
    console.log("MAP NOT DEFINED YET!!!");
  } else {
    if (svg.style.position === undefined) {
      svg.style.position = "absolute";
    } else {
      if (svg.style.position.localeCompare("absolute") != 0) {
        svg.style.position = "absolute";
      }
    }
    if (lastLoc !== undefined) {
      if (lastLoc.has(m.ID)) {
        // console.log("calculating new angle");
        // console.log(loc);
        // console.log(svg.style.left);
        // console.log(svg.style.top);
        // console.log(lastLoc.get(m.ID));
        // has a previous location of the agent
        /* The "-90" aligns the plane to math definition of 0deg (i.e. +x axis) */
        angle = calculateAngle(loc, lastLoc.get(m.ID).loc) - 270;
        // angle = angle - lastLoc.get(m.ID).angle;
        angle = -angle; // changes rotation to correspond to css definition
        // console.log(angle);
        // console.log('rotateY('.concat(angle.toString(),'deg)'));
        svg.style.transformOrigin = Math.round(overlay.latitude2Pixel(gcsmap.map,loc)).toString().
          concat(" ",Math.round(overlay.longitude2Pixel(gcsmap.map,loc)).toString());
        svg.style.transform = 'rotate('.concat(angle.toString(),'deg)');
        // svg.style.rotate = angle;
      }
    }
    // add proper event listener here to move the icon with the page moving
  }

  return angle;
}

function storeLastLoc(m, angle, loc) {
  if (lastLoc === undefined) {
    console.log("lastLoc is UNDEFINED!!");
  } else {
    if (angle === undefined) {
      lastLoc.set(m.ID, {loc, "angle": 0});
    } else {
      lastLoc.set(m.ID, {loc, "angle": angle});
    }
  }
}

function moveGCSIcon(m, document) {

}

function repositionAll(document) {
  console.log("Repositioning Icons");
  for (var icon of lastLoc) {
    console.log(icon);
    moveAgentIcon({"ID": icon[0], "Data": {"Lat": icon[1].loc.lat, "Lng": icon[1].loc.lng}}, document);
    breadcrumbs.reposition(document, icon[0]);
  }
}

module.exports = {
  agent: function(m, document) {
    loc = moveAgentIcon(m, document);
    angle = rotateAgentIcon(m, document);
    storeLastLoc(m, angle, loc);
  },
  gcs: function(m, document) {
    moveGCSIcon(m, document);
  },
  reposition: function(document) {
    repositionAll(document);
  }
};
