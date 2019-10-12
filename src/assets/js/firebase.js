const firebase = require("firebase/app");
require("firebase/auth");


function initFirebase(configLocation = __dirname + "../../env/env.firebase") {
  const firebaseConfig = require(configLocation);
  if (firebase.apps.length === 0) {
    firebase.initializeApp(firebaseConfig);
  }
}

function login(btn) {
  const email = document.getElementById('email').MDCTextField.value.toString();
  const password = document.getElementById('password').MDCTextField.value.toString();
  if (!email || !password) {
    showSnackbar('You must enter a email and password.');
    return;
  }
  btn.textContent = 'Logging you in...';
  btn.disabled = true;
  firebase.auth().signInWithEmailAndPassword(email, password).then((user) => {
    btn.textContent = 'Successful!';
    location.href = "./step4.html";
  }).catch(function(error) {
    if (error.code && error.message) {
      showSnackbar('Invalid username/password combo.');
      btn.textContent = 'Login';
      btn.disabled = false;
      // document.getElementById('password').MDCTextField.value = "";
    }
  });
}

function showSnackbar(text) {
  const snackbar = new mdc.snackbar.MDCSnackbar(document.querySelector('.mdc-snackbar'));
  snackbar.labelText = text;
  snackbar.open();
}

module.exports = { initFirebase, login };
