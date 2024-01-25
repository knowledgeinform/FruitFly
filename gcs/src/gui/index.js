// Modules to control application life and create native browser window
const {app, BrowserWindow, ipcMain, Tray, nativeImage} = require('electron');
// const nativeImage = require('electron').nativeImage;
const path = require('path');
const gcs = require('./backend/gcs');
const overlay = require('./overlay.js');
const register = require('./backend/register.js');
const message = require('./backend/message.js');
const data = require('./data.js');
// const {
//   Worker, isMainThread, parentPort, workerData
// } = require('worker_threads');
// const tester = require('./test.js');


// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;
// let client;

function getAppDir() {
  var appPathRaw = app.getAppPath();
  var appDir;
  console.log("getAppDir");
  if (appPathRaw.slice(-8).localeCompare("app.asar") === 0) {
    console.log("app asar found");
    appDir = appPathRaw.slice(0,-9);
  } else {
    console.log("NO app asar");
    appDir = appPathRaw;
  }
  console.log(appDir);
  return appDir;
}

ipcMain.on('reqBasePath', (e) => {
  // console.log("Got request; sending reply");
  e.returnValue = getAppDir();
  // console.log("Finished replying");
});

function registerOne() {
  cObj = {"self": "agent1", "color": register.setColor(1)};
  register.regEmitter.emit('registered',cObj);
}

function createWindow () {
  // on creating the window, start the gcs backend (note, this should probably
  // be started on application starting, rather than window creation, but as
  // long as this is just a one-window application, app-starting and window-creating
  // are synonomous.

  // addClientListeners(client);
  // console.log(data.data);

  // Create the browser window.
  // const appIcon = new Tray(path.join(__dirname, 'assets/icons/png/64x64.png'));

  mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegrationInWorker: true
    }
  });

  var image;
  if (process.platform === 'darwin') {
    image = nativeImage.createFromPath(path.join(__dirname, 'assets/icons/icon.png'));
    image.setTemplateImage(true);
    app.dock.setIcon(image);
  } else if (process.platform === 'win32') {
    image = nativeImage.createFromPath(path.join(__dirname, 'assets/icons/icon.ico'));
    image.setTemplateImage(true);
    mainWindow.setIcon(image);
  } else {
    // linux
    image = nativeImage.createFromPath(path.join(__dirname, 'assets/icons/icon.png'));
    image.setTemplateImage(true);
    mainWindow.setIcon(image);
  }

  // and load the index.html of the app.
  mainWindow.loadFile('index.html');
  console.log("loaded");
  // for testing
  // setTimeout(function () {
  //   registerOne();
  // }, 1000);
  // setTimeout(function () {
  //   tester.fireData();
  // }, 2000);

  gcs.start();

  // for testing:


  // currently, this code doesn't work, but it should
  // process.dlopen = () => {
  //   throw new Error('Load native module is not safe');
  // };
  // let worker = new Worker('./test.js');
  // worker.on('message', (value) => {
  //   console.log("worker message");
  //   console.log(value);
  // });
  // worker.on('error', (err) => {
  //   console.log("worker error");
  //   console.log(err);
  // });
  // worker.on('exit', (code) => {
  //   console.log("worker exited... code: "+code);
  //   if (code !== 0)
  //     new Error(`Worker stopped with exit code ${code}`);
  // });

  // overlay.initializeIconListeners(document);

  // Open the DevTools.
  // mainWindow.webContents.openDevTools();

  // console.log(document);

  // Emitted when the window is closed.
  mainWindow.on('closed', function () {
    gcs.stop();
    console.log("closed");
    // Dereference the window object, usually you would store windows
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    mainWindow = null;
  });
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', createWindow);

// Quit when all windows are closed.
app.on('window-all-closed', function () {
  console.log("all closed");
  gcs.stop();
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') app.quit();
});

app.on('activate', function () {
  console.log("activated");
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (mainWindow === null) createWindow();
});

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.

process.on('uncaughtException', () => {
  gcs.stop();
});
process.on('SIGTERM', () => {
  gcs.stop();
});

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
// start the ground control station server

register.regEmitter.on('registered', (clientObj) => {
  console.log("inside index js register event");
  console.log(clientObj);
  mainWindow.webContents.send('overlayClient', clientObj);
});

message.messageEmitter.on('received', (m) => {
  console.log("inside index js message event");
  // console.log(m);

  mainWindow.webContents.send('overlayMessage', m);
});
