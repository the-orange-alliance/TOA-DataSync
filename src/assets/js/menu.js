const { remote } = require('electron');
const { Menu, MenuItem } = remote;
const path = require("path");
const { saveLogs } = require(path.resolve('./src/assets/js/logger'));

const menu = new Menu();
menu.append(new MenuItem({ label: 'TOA DataSync', sublabel: 'Version' + remote.app.getVersion(), enabled: false}));
menu.append(new MenuItem({ label: 'Save Logs', click: saveLogs }));

window.addEventListener('contextmenu', (e) => {
  e.preventDefault();
  menu.popup({ window: remote.getCurrentWindow() })
}, false);
