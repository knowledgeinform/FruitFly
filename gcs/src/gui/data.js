var List = require("collections/list");
var Complex = require('complex.js');
const message = require('./backend/message.js');
// This file contains all the important data structures that are stored
// internally and plotted

var d = new Map();

function generateMessage(id, num, type, d) {
  var m = [];
  // console.log("id: "+id+" num: "+num+" type: "+type);
  // console.log(d);
  if (d != undefined && type != undefined) {
    if (type.localeCompare('Location') == 0) {
      // console.log("Location found");
      m[0] = {"ID": id, "Number": num, "DataType": type, "Data": d};
    } else {
      // console.log("Other found. d.length: "+Object.keys(d).length);
      for (var i = 0; i < Object.keys(d).length; i++) {
        if (Object.keys(d)[i].localeCompare("Time") != 0) {
          datum = {"Time": d.Time, "Dat": Object.values(d)[i]};
          m.push({"ID": id, "Number": num, "DataType": Object.keys(d)[i], "Data": datum});
        }
      }
    }
  } else {
    m[0] = {"ID": id, "Number": num, "DataType": type, "Data": d};
  }
  return m;
}

function parseMessage(rawMessage) {
  var elements = rawMessage.split(',');
  var keys = [elements.length];
  var values = [elements.length];

  for (var i = 0; i < elements.length; i++) {
    // console.log("element");
    // console.log(elements[i]);
    both = elements[i].split(':');
    if (both.length == 2) {
      keys[i] = both[0];
      values[i] = both[1];
      // console.log(keys[i]+" "+values[i]);
    } else if (both.length == 4) {
      keys[i] = both[0];
      values[i] = {"HH": parseInt(both[1]), "MM": parseInt(both[2]), "SS": parseInt(both[3])};
      // console.log(keys[i]+" "+values[i]);
    } else {
      console.log("INVALID KEY VALUE PAIR");
      console.log(both);
    }
  }
  var t, type;
  t = Date.now();
  switch(keys[0]) {
    case 'Conc':
      if (values.length != 8) {
        console.log("INVALID NUMBER OF FIELDS");
      } else {
        type = "Multiple";
        // console.log("Conc");
        // console.log("PAC2");
        // console.log(Complex(values[2].replace('j','i')));
        d = {"Time": t,
              "Concentration": parseFloat(values[0]),
              "PAC1": parseFloat(values[1]),
              "PAC2": Complex(values[2].replace('j','i')).abs(),
              "x": Complex(values[3].replace('j','i')).abs(),
              "y": Complex(values[4].replace('j','i')).abs(),
              "z": Complex(values[5].replace('j','i')).abs(),
              "L1": Complex(values[6].replace('j','i')).abs(),
              "L2": Complex(values[7].replace('j','i')).abs()};
      }
      break;
    case 'LAT':
    if (values.length != 4) {
      console.log("INVALID NUMBER OF FIELDS");
    } else {
      type = "Location";
      // console.log("lat");
      latmin = parseFloat(values[0].replace('N','').slice(3)) / 60.0 * 100.0;
      latmin = latmin.toString();
      // console.log(latmin);
      lat = values[0].replace('N','');
      // console.log(lat);
      lat = lat.slice(1,3).concat(latmin);
      // console.log(lat);
      // console.log("lng");
      lngmin = parseFloat(values[1].replace('0','-').replace('W','').slice(4)) / 60.0 * 100.0;
      lng = values[1].replace('0','-').replace('W','').slice(1,4).concat(lngmin.toString());
      // console.log(lng);
      // console.log("LAT");
      // console.log("HMSTime");
      // console.log(values[3]);
      d = {"Time": t,
            "Lat": parseFloat(lat) / 100,
            "Lng": parseFloat(lng) / 100,
            "Alt": parseFloat(values[2]),
            "HMSTime": values[3]};
    }
      break;
    default:
      console.log("UNKNOWN MESSAGE TYPE");
      type = "UNKNOWN";
      d = {"Time": t,
           "UNKNOWN": rawMessage};
  }
  var id = "agent1";
  var num = 0;
  return generateMessage(id,num,type,d);
}

function ParseData(data) {
  // console.log("Inside of ParseData");
  // console.log(data);
  var messages, id, num, type, d;
  messages = data.split(';');
  // console.log(messages);
  var m = [messages.length];
  for (var i = 0; i < messages.length; i++) {
    m[i] = parseMessage(messages[i]);
    for (var msingle of m[i]) {
      // console.log(msingle);
      message.messageEmitter.emit('received', msingle);
    }
  }
}

// the TimePoint function seems to be a bit archaic since as time-series data
// is received, all that's necessary is to add it to d like so:
// on registration:
//
// d["ETD Signal"].set(agentID, new Map())
//
// on message:
// d["ETD Signal"][agentID].set(i: {"i": d["ETD Signal"][agentID].size + 1, "t": timestamp, "d": datapoint});

// function TimePoint(time, dataPoint) {
//   this.t = time;
//   this.d = dataPoint;
// }

// threshold at which a third of the stored messages of a given type are
// written to a file and cleared from the map
// ~9.65e6 "messages" (roughly, 22 hrs of data)
// assumes a "message" = 80 bytes, and 2.33e9 bytes of space available
var indexThreshold = 90000000;
// clear a third of the "messages"; after the first 22 hrs, clears the map
// every 7.3 hrs
var messages2Clear = 30000000;

function store(obj, fileName) {
  var cacheDir = "cache";
  // var fullPath = cacheDir.concat("/",fileName,".jsonmap");
  var fullPath = fileName.concat(".jsonmap");
  var options = {encoding: 'utf8', flag: 'a'};
  var body = JSON.stringify(obj);
  try {
    fs.writeFileSync(fullPath, body, options);
  } catch (err) {
    console.log("ERROR! DID NOT WRITE TO FILE! "+fileName+".jsonmap");
    throw err;
  }

}

function clearList(list, fileName) {
  for (var i = 0; i < messages2Clear && list.length > 0; i++) {
    store(list.shift(), fileName);
  }
}

function checkAndAdd(stringID, type, datapoint) {
  return new Promise(resolve => {
    // console.log("Found ID: "+stringID+". Checking total length: "+d.get(type).get(stringID).t.length);
    // will need to do some heap usage checks and begin dumping old data onto
    // the disk
    if (d.get(type).get(stringID).t.length >= indexThreshold) {

      if (type.localeCompare("Location") == 0) {
        clearList(d.get(type).get(stringID).t, type.concat(stringID));
        clearList(d.get(type).get(stringID).lat, type.concat(stringID));
        clearList(d.get(type).get(stringID).lng, type.concat(stringID));
        clearList(d.get("Altitude").get(stringID).t, type.concat(stringID));
        clearList(d.get("Altitude").get(stringID).d, type.concat(stringID));
        // clearList(d.get("Speed").get(stringID), type.concat(stringID));
      } else {
        clearList(d.get(type).get(stringID).t, type.concat(stringID));
        clearList(d.get(type).get(stringID).d, type.concat(stringID));

      }
    }

    var i = d.get(type).get(stringID).t.length + 1;
    var key = i.toString();
    // console.log("New key: "+key);
    // console.log("datapoint:");
    // console.log(datapoint);
    if (type.localeCompare("Location") == 0) {
      // console.log("Storing type: "+type);
      d.get(type).get(stringID).t.push(datapoint.Time);
      d.get(type).get(stringID).lat.push(datapoint.Lat);
      d.get(type).get(stringID).lng.push(datapoint.Lng);
      d.get("Altitude").get(stringID).t.push(datapoint.Time);
      d.get("Altitude").get(stringID).d.push(datapoint.Alt);

      // calculate speed by central difference if not sent
      // if (d.get(type).get(stringID).t.length > 2) {
        // tm1 = d.get(type).get(stringID).t.peek().next.next;
        // tp1 = d.get(type).get(stringID).t.peek();
        // p1x =
        // p1y =
        // p1z =
        // p2x =
        // p2y =
        // p2z =
        // dx =
        // dy =
        // dz = p2z - p1z;
        // dl = Math.sqrt((dz)^2 + (dy)^2 + (dx)^2);
        // d.get("Speed").get(stringID).t.push(d.get(type).get(stringID).t.peek().next);

      // }

    } else {
      // console.log("Storing type: "+type);
      d.get(type).get(stringID).t.push(datapoint.Time);
      d.get(type).get(stringID).d.push(datapoint.Dat);
    }
    resolve("Added");
  });
}

async function addDataPoint(stringID, type, datapoint) {
  // console.log("Adding datapoint, checking type");
  if (d.has(type)) {
    // console.log("Found type: "+type+". Checking stringID");
    if (d.get(type).has(stringID)) {
      await checkAndAdd(stringID, type, datapoint);
    } else {
      console.log("CLIENT NOT IN DATABASE");
    }
  } else {
    console.log("DATA TYPE NOT IN DATABASE");
  }
}

function addToDataMap(stringID) {
  if (d === undefined) {
    console.log("Data Map UNDEFINED!!");
    console.log("Initializing");
    initialize();
  }
  // console.log("Adding "+stringID+" to data map");
  d.get("Concentration").set(stringID, {"t": new List(), "d": new List()});
  d.get("PAC1").set(stringID, {"t": new List(), "d": new List()});
  d.get("PAC2").set(stringID, {"t": new List(), "d": new List()});
  d.get("x").set(stringID, {"t": new List(), "d": new List()});
  d.get("y").set(stringID, {"t": new List(), "d": new List()});
  d.get("z").set(stringID, {"t": new List(), "d": new List()});
  d.get("L1").set(stringID, {"t": new List(), "d": new List()});
  d.get("L2").set(stringID, {"t": new List(), "d": new List()});
  d.get("Altitude").set(stringID, {"t": new List(), "d": new List()});
  // d.get("Speed").set(stringID, {"t": new List(), "d": new List()});
  d.get("Location").set(stringID, {"t": new List(), "lat": new List(), "lng": new List()});

}

function initialize() {
  // console.log("Initializing data");
  d.set("Concentration", new Map())
    .set("PAC1", new Map())
    .set("PAC2", new Map())
    .set("x", new Map())
    .set("y", new Map())
    .set("z", new Map())
    .set("L1", new Map())
    .set("L2", new Map())
    .set("Altitude", new Map())
    .set("Location", new Map());
    // x, y, z accel.
  console.log("Initialized data");
}

module.exports = {
  initialize: initialize,
  data: d,
  addToDataMap: addToDataMap,
  addDataPoint: addDataPoint,
  ParseData: ParseData
};
