const JSZip = require('jszip');
const Bowser = require('bowser');
const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS') || '[]');
let log = '';

exports.write = (...args) => {
  const date = `[${new Date().toISOString()}] `;
  const msg = args
    .map((msg) => (typeof msg === 'object' ? JSON.stringify(msg) : msg))
    .join(', ');
  log += date + msg + '\n';
};

exports.getLog = () => log;

exports.saveLogs = () => {
  let eventKey = 'Setup';
  if (events.length > 0 && events[0].toa_event_key) {
    eventKey = events[0].toa_event_key;
  }

  const fileName = `${eventKey} Logs.zip`;
  const zip = new JSZip();
  zip.file('local-storage.json', JSON.stringify(localStorage, null, 2));
  zip.file('browser.json', JSON.stringify(getBowserDetails(), null, 2));
  zip.file('console.txt', log);

  zip.generateAsync({ type: 'blob' }).then((blob) => {
    // Save the file with a new name
    const tag = document.createElement('a');
    tag.download = fileName;
    tag.href = URL.createObjectURL(blob);
    document.body.appendChild(tag);
    tag.click();
    tag.remove();
  });
};

function getBowserDetails() {
  const browser = Bowser.getParser(window.navigator.userAgent).getResult();
  return {
    ...browser,
    WebSocketSupport: !!window.WebSocket,
    ram: navigator.deviceMemory,
    onLine: navigator.onLine,
  };
}
