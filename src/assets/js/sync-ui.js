const { shell, clipboard } = require('electron');
const remote = require('electron').remote;
const fs = require('fs');
const JSZip = require('jszip');
const logger = require('./logger');
const apis = require('../../apis');
const appbar = require('./appbar');
const awardsUploader = require('./awards');
const toaApi = apis.toa;
const minScorekeeperVersion = apis.minScorekeeperVersion;
const index = parseInt(new URLSearchParams(window.location.search).get('i'), 10);
const configEvent = JSON.parse(localStorage.getItem('CONFIG-EVENTS'))[index];
const eventId = configEvent.event_id;
const eventKey = configEvent.toa_event_key;
const scorekeeperIp = localStorage.getItem('SCOREKEEPER-IP');
let shouldUpload = true;
let lastStatus = 'loading';

mdc.autoInit();
appbar.init();

function log(...args) {
  console.log(...args);
  logger.write(...args)
}

toaApi.get('/event/' + eventKey).then((data) => {
  const event = data.data[0];
  document.querySelector('#event-name').innerText = event.division_name ? event.division_name + ' Division' : event.event_name;
  document.querySelector('#view-event').onclick = () => {
    shell.openExternal('https://theorangealliance.org/events/' + event.event_key);
  };

  const shortLink = 'toa.events/' + eventKey;
  document.querySelector('#short-link').innerText = shortLink;
  document.querySelector('#copy-url').onclick = () => {
    clipboard.writeText('http://' + shortLink);
  };

  document.querySelector('#content').hidden = false;
}).catch((error) => {
  console.log(error.response);
  setStatus('no-internet')
});

document.querySelector('#start-sync-btn').onclick = () => {
  setShouldUpload(true);
};

document.querySelector('#stop-sync-btn').onclick = () => {
  setShouldUpload(false);
};

document.querySelector('#upload-awards').onclick = () => {
  awardsUploader();
};

document.querySelector('#settings-btn').onclick = () => {
  const dialog = document.querySelector('#settings-dialog').MDCDialog;
  document.querySelector('#ds-version').innerText = remote.app.getVersion() || '0.0.0';
  document.querySelector('#sk-version').innerText = localStorage.getItem('SCOREKEEPER-VERSION') || '0.0.0';
  dialog.open()
};

document.querySelector('#purge-data-btn').onclick = () => {
  const content = 'You are going to purge all the event data, when you upload the data again, the users might receive' +
    ' another notifications.\nAre you sure that you want to purge all the data?';
  showConfirmationDialog('Are you sure you want to purge all the data?', content).then(async () => {
    setShouldUpload(false);
    showSnackbar('Okay, purging...');

    // Delete localStorage
    for (const key in localStorage) {
      if (key.startsWith(`${eventId}-`)) {
        localStorage.removeItem(key);
      }
    }

    await toaApi.delete(`/event/${eventKey}/matches/all`);
    await toaApi.delete(`/event/${eventKey}/rankings`);
    await toaApi.delete(`/event/${eventKey}/awards`);
    await toaApi.delete(`/event/${eventKey}/teams`);
    showSnackbar('The data has been successfully purged.');
  });
};

document.querySelector('#save-logs-btn').onclick = () => {
  saveLogs();
};

document.querySelector('#dev-tools-btn').onclick = () => {
  showConfirmationDialog('Warning!', 'This is a feature intended for developers. If someone <u>who is not a TOA developer</u> told you to copy and paste' +
    ' something here, it is a probably scam and will give them access to your myTOA account and/or your Scorekeeper Software, ' +
    'including change data of your events.\nAre you sure that you want to open the dev console?').then(() => {
    remote.getCurrentWindow().openDevTools({mode: 'detach'});
  });
};

document.querySelector('#logout-btn').onclick = () => {
  showConfirmationDialog('Are you sure you want to logout?').then(() => {
    const divisions = JSON.parse(localStorage.getItem('CONFIG-EVENTS') || '[]');
    localStorage.clear();
    firebase.auth().signOut();
    if (divisions.length > 1 || divisions.length === 0) {
      remote.app.relaunch();
      remote.app.exit(0);
    } else {
      location.href = './setup-pages/step1.html';
    }
  });
};

function showConfirmationDialog(title, content) {
  const titleDiv = document.querySelector('#confirmation-dialog-title');
  const contentDiv = document.querySelector('#confirmation-dialog-content');
  return new Promise(function(resolve, reject) {
    titleDiv.innerText = title;
    contentDiv.innerHTML = content ? content.replace('\n', '<br/>') : '';
    contentDiv.hidden = !content;
    const dialog = document.querySelector('#confirmation-dialog').MDCDialog;
    dialog.listen('MDCDialog:closed', async (data) => {
      const action = data.detail.action;
      if (action === 'yes') {
        resolve();
      }
      reject();
    });
    dialog.open();
  });
}

function openStreamsDialog() {
  const dialog = document.querySelector('#streams-dialog').MDCDialog;
  const loading = document.querySelector('#streams-dialog-loading');
  const content = document.querySelector('#streams-dialog-content');
  const deleteStreamButton = document.querySelector('#delete-stream-btn');
  const addStreamButton = document.querySelector('#add-stream-btn');
  loading.hidden = false;
  content.hidden = true;
  deleteStreamButton.hidden = true;
  addStreamButton.hidden = true;
  toaApi.get(`/event/${eventKey}/streams`).then((data) => {
    const streams = data.data.filter((stream) => stream.is_active);
    if (streams.length > 0) {
      const stream = streams[0];
      const type = parseInt(stream.stream_type);
      const stringType = type && type === 0 ? 'youtube' : type && type === 1 ? 'twitch' : 'video';
      content.innerHTML = `Currently linked a stream.
            <div class="w-100 my-2">
                <div class="mdc-chip ${stringType}-chip" onclick="ui.openExternalLink('${stream.channel_url}')">
                    <i class="mdc-chip__icon mdc-chip__icon--leading mdi mdi-${stringType}"></i>
                    <div class="mdc-chip__text">${stream.channel_name || stream.url}</div>
                </div>
            </div>`;
      deleteStreamButton.onclick = () => unlinkStream(stream.stream_key, deleteStreamButton, dialog);
      deleteStreamButton.hidden = false;
    } else {
      content.innerHTML = `
            <div class="mdc-text-field w-100" data-mdc-auto-init="MDCTextField" id="stream-url">
                <input class="mdc-text-field__input">
                <div class="mdc-line-ripple"></div>
                <label class="mdc-floating-label">URL</label>
            </div>`;
      addStreamButton.hidden = false;
    }
    loading.hidden = true;
    content.hidden = false;
  }).catch(() => {
    dialog.close();
    showSnackbar('An error occurred while loading the streams.');
  });

  dialog.open();
  new Promise(resolve => setTimeout(resolve, 800)).then(() => {
    mdc.autoInit();
  });
}

document.querySelector('#streams-btn1').onclick = openStreamsDialog;
document.querySelector('#streams-btn2').onclick = openStreamsDialog;

function createStream(btn) {
  btn.textContent = 'Loading...';
  btn.disabled = true;
  toaApi.get(`/event/${eventKey}`).then((data) => {
    return data.data[0];
  }).then((event) => {
    const streamName = event.division_name ? event.event_name + ' - ' + event.division_name + ' Division' : event.event_name;
    // const radio = Array.from(document.querySelectorAll('#streams-dialog-content input[type=radio]'));
    // const streamTypeString = radio.length && radio.find(r => r.checked).value;
    const streamURL = document.querySelector('#stream-url').MDCTextField.value;
    const twitchRegex = new RegExp('^(?:https?:\\/\\/)?(?:www\\.|go\\.)?twitch\\.tv\\/([a-z0-9_]+)($|\\?)');
    const youtubeRegex = new RegExp('(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})');
    let streamLink, channelLink, channelName, streamType;
    if (twitchRegex.exec(streamURL)) {
      const channelId = twitchRegex.exec(streamURL)[1];
      streamLink = 'https://player.twitch.tv/?channel=' + channelId;
      channelLink = 'https://twitch.tv/' + channelId;
      channelName = channelId;
      streamType = 1;
    } else if (youtubeRegex.exec(streamURL)) {
      const vidId = youtubeRegex.exec(streamURL)[1];
      streamLink = 'https://www.youtube.com/embed/' + vidId;
      channelLink = 'https://www.youtube.com/watch?v=' + vidId;
      channelName = ''; // TODO: Implement Youtube API in here at some point
      streamType = 0;
    } else {
      return showSnackbar('Cannot find the stream info for this URL. Please note that we currently only support YouTube and Twitch streams.');
    }

    if (streamLink) {
      const toUpload = {
        stream_key: eventKey + '-LS1',
        event_key: eventKey,
        channel_name: channelName,
        stream_name: streamName,
        stream_type: streamType,
        is_active: true,
        url: streamLink,
        start_datetime: new Date(event.start_date).toJSON().slice(0, 19).replace('T', ' '),
        end_datetime: new Date(event.end_date).toJSON().slice(0, 19).replace('T', ' '),
        channel_url: channelLink
      };
      log('Uploading a stream...', toUpload);
      return toaApi.post(`/event/${eventKey}/streams`, JSON.stringify([toUpload])).then(() => {
        showSnackbar('The stream has been successfully uploaded.');
        document.querySelector('#streams-dialog').MDCDialog.close();
        btn.textContent = 'Add';
        btn.disabled = false;
      });
    }
  }).catch((error) => {
    log(error);
    showSnackbar('An error occurred while creating the stream.');
    btn.textContent = 'Add';
    btn.disabled = false;
  });
}

function unlinkStream(streamKey, btn, dialog) {
  btn.textContent = 'Unlinking...';
  btn.disabled = true;
  toaApi.delete(`/streams/${streamKey}`).then(() => {
    showSnackbar('The stream has been successfully unlinked.');
    btn.textContent = 'Delete';
    btn.disabled = false;
  }).catch(() => {
    showSnackbar('An error occurred while unlinking the stream.');
    btn.textContent = 'Delete';
    btn.disabled = false;
  });
}

function showChangeIpDialog() {
  const dialog = document.querySelector('#update-ip-dialog').MDCDialog;
  document.querySelector('#ip-address').MDCTextField.value = scorekeeperIp;
  dialog.open();
}

function updateIpAddress(btn) {
  const ipAddress = document.querySelector('#ip-address').MDCTextField.value;
  if (!ipAddress) {
    return;
  }
  btn.textContent = 'Loading...';
  btn.disabled = true;
  apis.scorekeeperFromIp(ipAddress).get('/v1/version').then((data) => {
    const version = data.data.version;
    console.log('Version ' + version);
    if (version < minScorekeeperVersion) {
      throw 'Your Scorekeeper version is too old, please use at least version ' + minScorekeeperVersion + '.';
    }
    localStorage.setItem('SCOREKEEPER-IP', ipAddress);
    localStorage.setItem('SCOREKEEPER-VERSION', version);
    remote.app.relaunch();
    remote.app.exit(0);
  }).catch((data) => {
    console.log(data);
    btn.textContent = 'Update';
    btn.disabled = false;
    showSnackbar(typeof data === 'string' ? data : 'Cannot access the scorekeeper.');
  });
}


function openExternalLink(url) {
  shell.openExternal(url);
}

// loading, ok, no-scorekeeper, no-internet, paused
function setStatus(status) {
  const header = document.querySelector('#status-header');
  const icon = document.querySelector('#status-icon');
  const title = document.querySelector('#status-text');
  const description = document.querySelector('#status-description');
  const content = document.querySelector('#content');
  const iconBase = 'mdc-top-app-bar__icon mdi mdi-';

  content.style.marginTop = status === 'ok' || status === 'no-scorekeeper' ? '150px' : '130px';

  if (status === 'loading') {
    header.className = 'mdc-top-app-bar';
    icon.className = iconBase + 'cloud-upload-outline';
    title.innerText = 'Connecting';
    description.innerText = 'We are connecting to our servers...';
  } else if (status === 'ok') {
    header.className = 'mdc-top-app-bar mdc-top-app-bar-sync-green';
    icon.className = iconBase + 'check-outline';
    title.innerText = 'All is good';
    description.innerText = 'Keep this window open or minimized and connected to the internet to continue upload.';
  } else if (status === 'no-scorekeeper') {
    header.className = 'mdc-top-app-bar mdc-top-app-bar-sync-red';
    icon.className = iconBase + 'cancel';
    title.innerText = 'Cannot access the Scorekeeper server';
    description.innerHTML = `Please make sure the Scorekeeper System is running at <code class="text-white">${scorekeeperIp}</code>`;
    description.innerHTML += `<br/>or <a class="link" id="update-ip">Change the Scorekeeper IP Address</a>.`;
    document.querySelector('#update-ip').onclick = showChangeIpDialog;
  } else if (status === 'no-internet') {
    header.className = 'mdc-top-app-bar mdc-top-app-bar-sync-red';
    icon.className = iconBase + 'wifi-strength-off-outline';
    title.innerText = 'No internet connection';
    description.innerText = 'Please make sure this computer is connected to the internet.';
  } else if (status === 'paused') {
    header.className = 'mdc-top-app-bar mdc-top-app-bar-sync-red';
    icon.className = iconBase + 'pause-circle-outline';
    title.innerText = 'The uploading is paused';
    description.innerText = 'You have paused the uploading.';
  }
  lastStatus = status;
}

function showSnackbar(text) {
  const snackbar = new mdc.snackbar.MDCSnackbar(document.querySelector('.mdc-snackbar'));
  snackbar.labelText = text;
  snackbar.open();
}

function getShouldUpload() {
  return shouldUpload;
}

function setShouldUpload(bool) {
  shouldUpload = bool;
  if (shouldUpload) {
    setStatus('loading');
    document.querySelector('#start-sync-btn').hidden = true;
    document.querySelector('#stop-sync-btn').hidden = false;
  } else {
    setStatus('paused');
    document.querySelector('#start-sync-btn').hidden = false;
    document.querySelector('#stop-sync-btn').hidden = true;
  }
}

function saveLogs() {
  const zip = new JSZip();
  zip.file("local-storage.json", JSON.stringify(localStorage, null, 2));
  zip.file("console.txt", logger.getLog());

  const window = remote.getCurrentWindow();
  const options = {
    title: "Save DataSync Logs",
    defaultPath : eventKey + " Logs.zip",
    buttonLabel : "Save logs",
    filters: [
      { name: 'DataSync Logs', extensions: ['zip'] }
    ]
  };
  remote.dialog.showSaveDialog(window, options, (path) => {
    if (!path) return;
    zip.generateNodeStream({ streamFiles:true })
      .pipe(fs.createWriteStream(path))
      .on('finish',  () => {
        console.log("Logs written.");
      });
  });
}

function setScheduleAccess(hide) {
  document.querySelector('#schedule-access').hidden = hide;
}

function openScorekeeperSchedule() {
  openExternalLink(`http://${scorekeeperIp}/event/${eventId}/dashboard/schedule/`);
}

module.exports = {
  lastStatus, setStatus, getShouldUpload, setShouldUpload, showSnackbar, openExternalLink, createStream, unlinkStream,
  updateIpAddress, openScorekeeperSchedule, setScheduleAccess
};
