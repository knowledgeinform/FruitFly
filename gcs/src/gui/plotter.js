// const Plotly = require('./node_modules/plotly.js-dist/plotly.js');
var dataManager = require('./data.js');
var List = require("collections/list");

var listofAgents = [],
    listOfPlottableTypes = [],
    currentAgent,
    currentPlType,
    currentData = [],
    currentTime = [];

var typeSelector, clientSelector;

var refreshRatePlotter_Alt = 10;
var refreshRatePlotter_Dat = 1;

var trace1 = {
  x: [1, 10],
  y: [0, 0],
  type: 'scatter',
  line: {color: 'rgb(0,0,255)'} // default line color (for first agent)
};

var trace2 = {
  x: [],
  y: [],
  type: 'scatter',
  line: {color: 'rgb(0,0,255)'} // default line color (for first agent)
};

var data = [trace1];

var layoutAltitude = {
  title: 'Altitude',
  font: {size: 10},
  margin: {l: 35, r: 5, t: 30, b: 30},
  yaxis: {title: {text: "Meters"}, range: [0, 500]}
};

var layoutPlotter = {
  title: 'Plotter',
  font: {size: 10},
  xaxis: {visible: true, contraintoward: "left", type: "date"},
  margin: {l: 50, r:25, t: 30, b:55},
  datarevision: 0
};

function getYData(d, type, chosenClient) {
  allData = d.get(type).get(chosenClient).toArray();
  currentY = [];
  currentTime = [];
  for (var i = 0 ; i < allClientNames.length ; i++){
    if ( allClientNames[i] === chosenClient ) {
      currentY.push(allGdp[i]);
      currentTime.push(allYear[i]);
    }
  }
}

// function deSerialize(l) {
//   cur = l.head.next;
//
//   while (cur != null) {
//     val = cur.value
//     cur = cur.next
//   }
// }

function updatePlots(m,d) {
  return new Promise(resolve => {
    // console.log(typeSelector.value);
    // console.log(clientSelector.value);
    if (m.DataType.localeCompare("Location") == 0) {
      if (m.ID.localeCompare(clientSelector.value) == 0) {
        trace1.y = [m.Data.Alt, m.Data.Alt];
        Plotly.react('altitude', [trace1], layoutAltitude, {responsive: true});
      }
      if (m.ID.localeCompare(clientSelector.value) == 0 &&
          typeSelector.value.localeCompare("Altitude") == 0) {
        trace2.x.push(m.Data.Time);
        trace2.y.push(m.Data.Alt);
        layoutPlotter.datarevision = layoutPlotter.datarevision + 1;
        if ((layoutPlotter.datarevision % refreshRatePlotter_Alt) == 0) {
          Plotly.react('plotter', [trace2], layoutPlotter, {responsive: true});
        }
      }
      // console.log(d.get("Altitude").get(m.ID).t.toArray());
      // console.log(d.get("Altitude").get(m.ID).d.toArray());
    } else {
      if (m.ID.localeCompare(clientSelector.value) == 0 &&
          m.DataType.localeCompare(typeSelector.value) == 0) {
        trace2.x.push(m.Data.Time);
        trace2.y.push(m.Data.Dat);
        layoutPlotter.datarevision = layoutPlotter.datarevision + 1;
        if ((layoutPlotter.datarevision % refreshRatePlotter_Dat) == 0) {
          Plotly.react('plotter', [trace2], layoutPlotter, {responsive: true});
        }
      }
      // console.log(d.get(m.DataType).get(m.ID).t.toArray());
      // console.log(d.get(m.DataType).get(m.ID).d.toArray());
    }
    resolve(true);
  });
}

function assignOptions(textArray, document, docID) {
    selector = document.getElementById(docID);
    for (var i = 0; i < textArray.length;  i++) {
        var currentOption = document.createElement('option');
        if (textArray[i].localeCompare('Location') != 0) {
          currentOption.text = textArray[i];
          selector.appendChild(currentOption);
        }
    }
}

function replot() {
  // console.log(typeSelector.value);
  // console.log(clientSelector.value);
  if (document.getElementById(clientSelector.value.concat("-icon")) != null) {
    trace2.line = {color: document.getElementById(clientSelector.value.concat("-icon")).style.color};
    trace1.line = {color: document.getElementById(clientSelector.value.concat("-icon")).style.color};
  }
  if (dataManager.data.get(typeSelector.value).has(clientSelector.value)) {
    trace2.x = dataManager.data.get(typeSelector.value).get(clientSelector.value).t.toArray();
    trace2.y = dataManager.data.get(typeSelector.value).get(clientSelector.value).d.toArray();
    layoutPlotter.datarevision = 0;

    Plotly.update('plotter', [trace2], layoutPlotter, {responsive: true});
    lastAlt = dataManager.data.get("Altitude").get(clientSelector.value).d.pop();
    dataManager.data.get("Altitude").get(clientSelector.value).d.push(lastAlt);

    trace1.y = [lastAlt, lastAlt];
    Plotly.update('altitude', [trace1], layoutAltitude, {responsive: true});
  }

}

module.exports = {
  start: function (d, document) {
    var keys = [ ...d.keys() ];
    console.log(keys);
    assignOptions(keys, document, "plot-selector");
    typeSelector = document.querySelector('.plottype');
    clientSelector = document.querySelector('.agentid');
    Plotly.newPlot('plotter', [trace2], layoutPlotter, {responsive: true});
    Plotly.newPlot('altitude', [trace1], layoutAltitude, {responsive: true});

    typeSelector.addEventListener('change',replot, false);
    clientSelector.addEventListener('change',replot, false);
  },
  update: async function(m, d) {
    await updatePlots(m,d);
    // console.log("Inside map update");
    // for "plotter", check which plot is being displayed and only update the
    // one being displayed. Update the others when this function is called and
    // they're the highlighted plot
    // var d = data.data;
    // console.log(m);
    // console.log(d);
    // console.log(list);
    // type = m.DataType;
    // chosenClient = m.ID;
    // allData = list.toArray();
    // console.log(allData);
  },
  updateOptions: assignOptions
};
