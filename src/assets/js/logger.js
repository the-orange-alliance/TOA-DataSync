const JSZip = require('jszip');
const index = parseInt(new URLSearchParams(window.location.search).get('i'), 10);
const configEvent = JSON.parse(localStorage.getItem('CONFIG-EVENTS') || '[]');

let log = '';

exports.write = (...args) => {
  const date = `[${new Date().toISOString()}] `;
  const msg = args.map(msg => typeof msg === 'object' ? JSON.stringify(msg) : msg).join(', ');
  log += date + msg + '\n';
};

exports.getLog = () => log;

exports.saveLogs = () => {
  let eventKey = 'Setup';
  if (configEvent.length > 0 && !isNaN(index) && configEvent[index] && configEvent[index].toa_event_key) {
    eventKey = configEvent[index].toa_event_key
  }

  const fileName = `${eventKey} Logs.zip`;
  const zip = new JSZip();
  zip.file('local-storage.json', JSON.stringify(localStorage, null, 2));
  zip.file('console.txt', log);

  zip.generateAsync({type: 'blob'}).then((blob) => {
    // Save the file with a new name
    const tag = document.createElement('a');
    tag.download = fileName;
    tag.href = blob;
    document.body.appendChild(tag);
    tag.click();
    tag.remove();
  });
};
