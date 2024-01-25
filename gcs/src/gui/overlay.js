const gcsmap = require('./gcsmap.js');
const util = require('util');
// const register = require('./backend/register.js');

function iconSrc(base,size) {
  var name = base.concat("-",size,".svg");
  var basePath = "./sprites/_svg/";
  return basePath.concat(name);
}

Math.radians = function(degrees) {
  return degrees * Math.PI / 180;
};

// Converts from radians to degrees.
Math.degrees = function(radians) {
  return radians * 180 / Math.PI;
};

// earth's radius, (what Google uses for Google Earth)
// https://www.movable-type.co.uk/scripts/latlong.html
var r = 6378137; // meters

function lat2Pixel(tile,loc) {
  // latitude and longitude in degrees
  // since we're only concerned about differences, just use straight spherical-
  // to-cartesian formula
  // console.log(gcsmap.map);
  // console.log("lat2pixel");
  // console.log(tile);
  // console.log(loc);
  var y1 = r * Math.sin(Math.radians(tile.TLcorner.lat)) * Math.sin(Math.radians(tile.TLcorner.lng));
  var y2 = r * Math.sin(Math.radians(loc.lat)) * Math.sin(Math.radians(loc.lng));
  var y3 = r * Math.sin(Math.radians(tile.BRcorner.lat)) * Math.sin(Math.radians(tile.BRcorner.lng));
  var dy_agent = y2 - y1; // meters
  var dy_screen = y3 - y1;
  var res = dy_agent / dy_screen * tile.height + gcsmap.map.tOffset;
  // console.log(dy_agent);
  // console.log(dy_screen);
  // console.log(res);
  return res;
}

function lng2Pixel(tile,loc) {
  // latitude and longitude in degrees
  // since we're only concerned about differences, just use straight spherical-
  // to-cartesian formula
  // console.log("lng2pixel");
  // console.log(gcsmap.map);
  // console.log(tile);
  // console.log(loc);
  var x1 = r * Math.sin(Math.radians(tile.TLcorner.lat)) * Math.cos(Math.radians(tile.TLcorner.lng));
  var x2 = r * Math.sin(Math.radians(loc.lat)) * Math.cos(Math.radians(loc.lng));
  var x3 = r * Math.sin(Math.radians(tile.BRcorner.lat)) * Math.cos(Math.radians(tile.BRcorner.lng));
  var dx_agent = x2 - x1;
  var dx_screen = x3 - x1;
  var res = dx_agent / dx_screen * tile.width + gcsmap.map.lOffset;
  // console.log(dx_agent);
  // console.log(dx_screen);
  // console.log(res);
  return res;
}

// latitude and longitude in degrees

function gpsDistance(tile,loc) {
  // point 1: top-left corner of tile
  // point 2: loc
  var phi_1 = Math.radians(tile.TLcorner.lat);
  var phi_2 = Math.radians(loc.lat);
  var d_phi = Math.radians(loc.lat - tile.TLcorner.lat);
  var d_lambda = Math.radians(loc.lng - tile.TLcorner.lng);

  var a = Math.sin(d_phi/2) * Math.sin(d_phi/2) + Math.cos(phi_1) * Math.cos(phi_2) * Math.sin(d_lambda/2) * Math.sin(d_lambda/2);
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  var distance = r * c;

}

function hideOtherIcons(document) {
  var svg = document.getElementById('agent-icon');
  svg.style.visibility = "hidden";
}

module.exports = {
  home: function (gcsLoc, document, map, gcsTile) {
    console.log("overlay home");
    // console.log(gcsLoc);
    var svg = document.getElementById('gcs-icon');
    svg.style.left = Math.round(lng2Pixel(map,gcsLoc)).toString().concat("px");
    svg.style.top = Math.round(lat2Pixel(map,gcsLoc)).toString().concat("px");
    // console.log(svg.offsetLeft);
    // add the proper event listener here to move the icon with the page moving
    // hideOtherIcons(document);
  },
  latitude2Pixel: lat2Pixel,
  longitude2Pixel: lng2Pixel
};
