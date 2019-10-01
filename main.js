const { app, BrowserWindow, Menu } = require('electron');
const { platform } = require('os');

const isProd = process.env.NODE_ENV === "production";

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let win;

function createWindow () {
  // Create the browser window.
  win = new BrowserWindow({
    frame: platform() === 'darwin',
    width: 500,
    height: 640,
    resizable: false,
    titleBarStyle : 'hiddenInset',
    maximizable: false,
    fullscreenable: false,
    show: false,
    title: 'DataSync',
    icon: __dirname + '/AppIcon.ico',
    webPreferences: {
      nodeIntegration: true
    }
  });

  // and load the index.html of the app.
  win.loadFile('src/index.html');

  // Emitted when the window is closed.
  win.on('closed', () => {
    win = null
  });

  win.once('ready-to-show', () => {
    win.show()
  })
}

app.on('ready', createWindow);

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (win === null) {
    createWindow();
  }
});


const template = [
  // { role: 'appMenu' }
  ...(process.platform === 'darwin' ? [{
    label: app.getName(),
    submenu: [
      { role: 'about' },
      { role: 'quit' }
    ]
  }] : []),
  // { role: 'editMenu' }
  {
    label: 'Edit',
    submenu: [
      { role: 'undo' },
      { role: 'redo' },
      { type: 'separator' },
      { role: 'cut' },
      { role: 'copy' },
      { role: 'paste' },
      { role: 'delete' },
      { role: 'selectAll' }
    ]
  },
  {
    role: 'help',
    submenu: [
      {
        label: 'Discord Server',
        click () { require('electron').shell.openExternalSync('https://discord.gg/5fH66UV') }
      },
      {
        label: 'Email Us',
        click () { require('electron').shell.openExternalSync('mailto:contact@theorangealliance.org') }
      },
      {
        label: 'Learn More',
        click () { require('electron').shell.openExternalSync('http://theorangealliance.org') }
      }
    ]
  }
];

if (isProd) {
  const menu = Menu.buildFromTemplate(template);
  Menu.setApplicationMenu(menu);
}
