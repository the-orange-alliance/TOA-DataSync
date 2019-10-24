const { firebase } = require('./assets/js/firebase.js');

firebase.auth().onAuthStateChanged((user) => {
  const ipAddress = localStorage.getItem('SCOREKEEPER-IP');
  const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
  if (!ipAddress) {
    return location.href = './setup-pages/step1.html';
  } else if (ipAddress && !events) {
    return location.href = './setup-pages/step2.html';
  } else if (ipAddress && events && !user) {
    return location.href = './setup-pages/step3.html';
  } else if (ipAddress && events && user) {
    for (const event of events) {
      if (!event.toa_event_key || !event.toa_api_key) {
        return location.href = './setup-pages/step4.html';
      }
    }

    // SETUP DONE :)

    // TODO: Fix Dual Divisions support
/*
    for (let i = 1; i < events.length; i++) {
      const win = new BrowserWindow({
        frame: platform() === 'darwin',
        width: 500,
        height: 640,
        x: i % 2 === 0 ? 0 : screen.width - 500,
        y: (screen.height - 640) / 2,
        resizable: false,
        titleBarStyle : 'hiddenInset',
        maximizable: false,
        fullscreenable: false,
        webPreferences: {
          nodeIntegration: true
        }
      });
      win.loadURL(`file://${__dirname}/sync.html?i=${i}`)
    }
*/

    return location.href = './sync.html?i=0';
  }
});
