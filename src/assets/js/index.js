const { firebase } = require('./firebase.js');
require('devtools-detect');

window.addEventListener('devtoolschange', event => {
  if (event.detail.isOpen) {
    const warningStyle = 'font-family: sans-serif; font-size: 20px;';
    const warningText1 = 'This is a feature intended for developers. If someone ';
    const warningText2 = 'who is not a TOA developer';
    const warningText3 = ' told you to copy and paste something here, it is probably a scam and will give them ' +
      'access to your myTOA account and/or your Scorekeeper Software.';

    console.log('%c Welcome to the DataSync ', 'background: #F89808;color: black;font-size:40px;font-weight: bold;font-family: sans-serif');
    console.log(`%c${warningText1}%c${warningText2}%c${warningText3}`,
      warningStyle, warningStyle + 'font-weight: bold;text-decoration: underline;', warningStyle);
  }
});

firebase.auth().onAuthStateChanged((user) => {
  const ipAddress = localStorage.getItem('SCOREKEEPER-IP');
  const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
  let newPath = location.pathname;
  if (!ipAddress || !events || !user) {
    newPath = '/setup.html';
  } else {
    newPath = '/sync.html';

    for (const event of events) {
      if (!event.toa_event_key || !event.toa_api_key) {
        newPath = '/setup.html';
      }
    }
  }

  if (location.pathname !== newPath) {
    location.pathname = newPath;
  }
});

if (dataSyncMode !== 'development') {
  window.onbeforeunload = (e) => {
    const text = 'You are going to stop uploading data to The Orange Alliance. Are you sure?';
    e = e || window.event;
    if (e) e.returnValue = text;
    return text;
  };
}
