const firebase = require('firebase/app');
require('firebase/auth');
const firebaseConfig = require('../env/env.firebase');

if (firebase.apps.length === 0) {
  firebase.initializeApp(firebaseConfig);
}

module.exports = { firebase };
