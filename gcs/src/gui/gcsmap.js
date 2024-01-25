const fs = require('fs');
const gcs = require('./backend/gcs.js');
const util = require('util');
const path = require('path');

var basePath;

function loadTileList(tilePathDir) {
  var tilePath = tilePathDir.concat("TileList.json");
  var tileListObj;
  tileListObj = undefined;
  try {
    tileListObj = JSON.parse(fs.readFileSync(tilePath, 'utf8'));
  } catch(e) {
    console.log('Error:', e.stack);
  }
  return tileListObj;
}

function findTile(gcsLoc) {
  var center = {
    lat: (gcsLoc.lat + gcsLoc.centerOffset.latOff),
    lng: (gcsLoc.lng + gcsLoc.centerOffset.lngOff)
  };

  var tilePathDir = path.join(basePath,"tiles/Z/XY/");

  var tileList = loadTileList(tilePathDir);
  // console.log(tileList.tiles.length);
  for (var tile of tileList.tiles) {
    // console.log(util.inspect(tile, {showHidden: false, depth: null}));
    // console.log(typeof tile.BRcorner.lat);
    // console.log(tile.BRcorner.lat);
    // console.log(typeof center.lat);
    // console.log(center.lat);
    // console.log(tile.BRcorner.lng > center.lng);
    // assumes northern hemisphere (lat inequalities)
    // assumes west of the prime meridian (lng inequalities)
    if ((tile.TLcorner.lat > center.lat && tile.BRcorner.lat < center.lat) &&
        (tile.TLcorner.lng < center.lng && tile.BRcorner.lng > center.lng)) {
          console.log("Found tile");
          return {src: tilePathDir.concat(tile.PathName),
                  tile};
        }
  }
  return {src: tilePathDir.concat(tileList.error.PathName),
          "tile": tileList.error};
}

class MapC {
  constructor(tOffset, lOffset, tlID, totalWidth, totalHeight, TLcorner, BRcorner) {
    this.tOffset = tOffset;
    this.lOffset = lOffset;
    this.tlID = tlID;
    this.width = totalWidth;
    this.height = totalHeight;
    this.TLcorner = TLcorner;
    this.BRcorner =  BRcorner;
  }
}

var m = new MapC();

function rasterize(document, gcsLoc) {
  // need to find out the typical pixels/latitude(longitude) to determine the
  // number of tiles to rasterize

  // var numTiles = determineNumberOfTiles(document);

  // expand this function to find all the necessary tiles
  var tile = findTile(gcsLoc);
  // console.log(tileSrc);
  // console.log(util.inspect(tile, {showHidden: false, depth: null}));

  // For loop that sums across tile widths and tile heights
  // map top left is first tile,
  // map bottom right is last tile
  var elem = document.createElement("img");
  elem.src = tile.src;
  elem.height = window.innerHeight - document.getElementById("plotdiv").clientHeight;
  elem.width = tile.tile.width * elem.height / tile.tile.height;
  elem.id = tile.tile.PathName;
  elem.alt = 'location';
  document.getElementById('map').insertBefore(elem,document.getElementById('gcs-icon'));
  var firstTile = tile;
  var tlID = elem.id;
  var tOffset = elem.offsetTop;
  var lOffset = elem.offsetLeft;
  var lastTile = tile;

  var totalWidth = elem.width;
  var totalHeight = elem.height;

  // MapC(tOffset, lOffset, totalWidth, totalHeight, firstTile.tile.TLcorner, lastTile.tile.BRcorner);
  m.tOffset = tOffset;
  m.lOffset = lOffset;
  m.tlID = tlID;
  m.width = totalWidth;
  m.height = totalHeight;
  m.TLcorner = firstTile.tile.TLcorner;
  m.BRcorner = lastTile.tile.BRcorner;
  return m;

}

function resizeMap(document) {
  console.log("Resizing map");
  var elem = document.getElementById("map");
  var mapImg = document.getElementById(m.tlID);
  newHeight = window.innerHeight - document.getElementById("plotdiv").clientHeight;
  // console.log("Current width "+mapImg.width+" new width "+Math.round(m.width * newHeight / m.height));
  // console.log("Current height "+mapImg.height+" new height "+newHeight);

  mapImg.height = newHeight;
  mapImg.width = Math.round(m.width * newHeight / m.height);
  m.width = mapImg.width;
  m.height = mapImg.height;
}

module.exports = {
  map: m,
  mapInit: function (document) {
    tempM = rasterize(document, gcs.loc());
    console.log("initialized map");
    return tempM;
  },
  findTile: function(loc) {
    return findTile(loc);
  },
  resizeMap: function (document) {
    resizeMap(document);
  },
  initializeBasePath: function(bp) {
    console.log("gcsmap: inializing basepath: "+bp);
    basePath = bp;
  }
};
