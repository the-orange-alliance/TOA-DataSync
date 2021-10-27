const apis = require('./apis');
const logger = require('./logger');
const { firebase } = require('./firebase');
const minScorekeeperVersion = apis.minScorekeeperVersion;

let apiKeyRetryAttempts = 0;

mdc.autoInit();

const showStep = (id) => {
  for (let i = 1; i <= 5; i++) {
    const elm = document.querySelector('#step' + i);
    if (i === id) {
      elm.classList.add('toa-stepper-step--active');
    } else {
      elm.classList.remove('toa-stepper-step--active');
    }
  }
};

document.querySelector('#sk-recommend-version').textContent = apis.recommendScorekeeperVersion;
document.querySelector(
  '#sk-download-link'
).href = `https://github.com/FIRST-Tech-Challenge/scorekeeper/releases/tag/${apis.scorekeeperReleaseTag}`;

function log(...args) {
  console.log(...args);
  logger.write(...args);
}

function getEventsFromFirebase() {
  let isAdmin = false;
  let adminEvents = [];
  firebase.auth().onAuthStateChanged(async (user) => {
    if (user) {
      await user
        .getIdTokenResult()
        .then(async (value) => {
          return await apis
            .cloud(value.token)
            .get('/user', {
              headers: {
                short: true
              }
            })
            .then((me) => {
              if (me.data.level >= 6) {
                isAdmin = true;
              } else {
                throw 'non-admin user';
              }
            })
            .catch(async () => {
              return await apis
                .cloud(value.token)
                .get('/getAllCurrentWriteEvents')
                .then((data) => {
                  adminEvents = data.data.filter((event) => event.season_key === apis.config.toaSeason);
                  adminEvents.sort((a, b) => {
                    const date1 = new Date(a.start_date).getTime();
                    const date2 = new Date(b.start_date).getTime();
                    return date1 - date2;
                  });
                })
                .catch((error) => {
                  if (error) {
                    console.error(error);
                    showSnackbar('An error has occurred, please reload the and try again.');
                    log('Error in getting cloud');
                  }
                });
            });
        })
        .catch((error) => {
          if (error.code && error.message) {
            console.error(error);
            showSnackbar('An error has occurred, please restart the app and try again.');
            log("Couldn't get token");
          }
        });

      const content = document.querySelector('#cards');
      const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
      events.forEach((event) => {
        let html = '';
        html += `<div class="mdc-card ${events[events.length - 1] !== event ? 'mb-3' : ''}">
          <div class="card__primary">
            <h2 class="card__title mdc-typography mdc-typography--headline6" style="font-size: 1rem; line-height: 1rem;">Select the TOA Event for <i>${
              event.name
            } ${events.length > 1 ? 'Division' : ''}</i></h2>
          </div>`;

        if (isAdmin) {
          html += `<div class="mdc-text-field mdc-text-field--no-label" data-mdc-auto-init="MDCTextField" data-event-input>
            <input type="text" class="mdc-text-field__input"
             style="text-transform: uppercase" placeholder="Event Key" oninput="setup.onEventKeyChanged()">
            <div class="mdc-line-ripple"></div>
          </div>`;
        } else {
          html += `<div class="mdc-select event-select w-100" style="height: 52px" data-mdc-auto-init="MDCSelect"
            data-event-input data-event-id="${event.event_id}">
            <i class="mdc-select__dropdown-icon"></i>
            <div class="mdc-select__selected-text setup-event-select">Select an event...</div>
            <div class="mdc-select__menu mdc-menu mdc-menu-surface">
              <ul class="mdc-list">`;
          adminEvents.forEach((event) => {
            html += `<li class="mdc-list-item"  data-value="${event.event_key}"><span>${event.event_name}
              ${event.division_name ? `<span style="font-weight: 500"> - ${event.division_name} Division</span>` : ``}
            </span></li>`;
          });
          html += `</ul>
            </div>
            <div class="mdc-line-ripple"></div>
          </div>`;
        }
        html += '</div>';
        content.innerHTML += html;
      });
      if (isAdmin) {
        content.innerHTML += `<div class="mdc-form-field mt-1">
          <div class="mdc-checkbox">
            <input type="checkbox" class="mdc-checkbox__native-control" id="live-checkbox"/>
            <div class="mdc-checkbox__background">
              <svg class="mdc-checkbox__checkmark" viewBox="0 0 24 24">
                <path class="mdc-checkbox__checkmark-path" fill="none" d="M1.73,12.91 8.1,19.28 22.79,4.59"/>
              </svg>
              <div class="mdc-checkbox__mixedmark"></div>
            </div>
          </div>
          <label for="live-checkbox">Live Uploading</label>
        </div>`;
      }
      mdc.autoInit();
      showStep(5);
      Array.from(document.querySelectorAll('[data-mdc-auto-init="MDCSelect"]')).forEach((input) => {
        input[input.dataset.mdcAutoInit].listen('MDCSelect:change', onEventKeyChanged);
      });
      Array.from(document.querySelectorAll('.mdc-menu.mdc-select__menu')).forEach((input) => {
        input.style.width = document.querySelector('.mdc-select').clientWidth + 'px';
      });
    } else {
      showSnackbar('An error has occurred, please log in again.');
    }
  });
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
  firebase
    .auth()
    .signInWithEmailAndPassword(email, password)
    .then((user) => {
      btn.textContent = 'Successful!';
      setTimeout(() => {
        btn.textContent = 'Please wait...';
      }, 2000);
      getEventsFromFirebase(); // We'll call showStep  the events load
    })
    .catch((error) => {
      if (error.code && error.message) {
        showSnackbar('Invalid username/password combo.');
        btn.textContent = 'Next';
        btn.disabled = false;
        // document.getElementById('password').MDCTextField.value = "";
      }
    });
}

function selectedToaEvent(btn) {
  btn.textContent = 'Loading...';
  btn.disabled = true;
  btn.onclick = null;
  const setInvalid = () => {
    btn.textContent = 'Start Sync';
    onEventKeyChanged(); // Removes the disable
  };

  const inputs = Array.from(document.querySelectorAll('[data-event-input]'));
  const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
  for (let i = 0; i < events.length; i++) {
    events[i].toa_event_key = inputs[i][inputs[i].dataset.mdcAutoInit].value.toUpperCase();
  }

  firebase.auth().onAuthStateChanged((user) => {
    if (user === null) {
      showSnackbar('An error has occurred. Please reload the page and try again.');
    } else {
      user
        .getIdToken()
        .then(async (token) => {
          let hasErrors = false;
          for (const event of events) {
            const eventKey = event.toa_event_key;
            await getApiKey(token, eventKey)
              .then((apiKey) => {
                event.toa_api_key = apiKey;
              })
              .catch((error) => {
                log(error);
                setInvalid();
                if (error.response.status === 403) {
                  showSnackbar('Your access for this event has expired.');
                } else if (error.response.status === 404) {
                  showSnackbar('Event not found.');
                } else {
                  showSnackbar(
                    error.response.data['_message'] ||
                      'Cannot access TOA servers. Please make sure you have an Internet connection.'
                  );
                }
                hasErrors = true;
              });
          }
          if (!hasErrors) {
            for (const event of events) {
              if (!event.toa_event_key || !event.toa_api_key) {
                return showSnackbar('An error occurred while receiving an API Key.');
              }
            }
            const liveCheckbox = document.querySelector('#live-checkbox');
            await Promise.all(
              events.map((e) =>
                apis.toa(e.toa_api_key).post('/connect', {
                  event_key: e.toa_event_key,
                  source: {
                    key: !liveCheckbox || liveCheckbox.checked ? 1 : 2,
                    name: `DataSync v${dataSyncVersion || '0.0.0'}`
                  },
                  user: {
                    name: user.displayName,
                    email: user.email,
                    uid: user.uid
                  }
                })
              )
            );
            btn.textContent = 'Successful!';
            localStorage.setItem('CONFIG-EVENTS', JSON.stringify(events));
            location.href = './index.html';
          }
        })
        .catch((e) => {
          showSnackbar('An Error has occurred. Please reload the page and try again.');
          setInvalid();
          throw e;
        });
    }
  });
}

function getApiKey(token, eventKey) {
  return new Promise((resolve, reject) => {
    apis
      .cloud(token)
      .get('/getAPIKey', {
        headers: { data: eventKey },
        body: { generate: true }
      })
      .then((data) => {
        const apiKey = data.data.key;
        log(data.data);
        return apis
          .toa(apiKey)
          .get('/event/' + eventKey)
          .then(() => {
            resolve(apiKey);
          })
          .catch((error) => {
            reject(error);
          });
      });
  });
}

function onEventKeyChanged() {
  const inputs = Array.from(document.querySelectorAll('[data-event-input]'));
  let isValid = true;
  for (const input of inputs) {
    const value = input[input.dataset.mdcAutoInit].value;
    if (!value || value.trim().length === 0 || value.split('-').length !== 3) {
      isValid = false;
    }
  }
  const btn = document.querySelector('#start-sync');
  btn.disabled = !isValid;
  btn.onclick = isValid ? () => selectedToaEvent(btn) : null;
}

function testScorekeeperConfig(btn) {
  const ipAddress = document.getElementById('ip-address').MDCTextField.value;
  if (!ipAddress) {
    return;
  }
  btn.textContent = 'Loading...';
  btn.disabled = true;
  apis
    .scorekeeperFromIp(ipAddress)
    .get('/v1/version/')
    .then((data) => {
      const version = data.data.version;
      log('Version ' + version);
      if (version < minScorekeeperVersion) {
        throw 'Your Scorekeeper version is too old, please use at least version ' + minScorekeeperVersion + '.';
      }
      localStorage.setItem('SCOREKEEPER-IP', ipAddress);
      localStorage.setItem('SCOREKEEPER-VERSION', version);
      showStep(2);
      loadScorekeeperEvents();
    })
    .catch((data) => {
      log(data);
      btn.textContent = 'Retry';
      btn.disabled = false;
      showSnackbar(typeof data === 'string' ? data : 'Cannot access the scorekeeper.');
    });
}

function loadScorekeeperEvents() {
  const skHost = localStorage.getItem('SCOREKEEPER-IP');
  const scorekeeperApi = apis.scorekeeperFromIp(skHost);
  scorekeeperApi
    .get('/v1/events/')
    .then(async (data) => {
      const events = [];
      for (const eventId of data.data.eventCodes) {
        try {
          const event = (await scorekeeperApi.get('/v1/events/' + eventId + '/')).data;
          if (eventId.endsWith('_0') && event.finals) {
            const baseEventId = eventId.substring(0, eventId.length - 2);
            const division1 = (await scorekeeperApi.get('/v1//events/' + baseEventId + '_1/')).data;
            const division2 = (await scorekeeperApi.get('/v1/events/' + baseEventId + '_2/')).data;

            let data = {
              event_id: eventId,
              name: event.name,
              type: event.type,
              divisions: [
                {
                  event_id: eventId,
                  name: 'Finals',
                  type: event.type
                },
                {
                  event_id: division1.eventCode,
                  name: division1.name,
                  type: division1.type
                },
                {
                  event_id: division2.eventCode,
                  name: division2.name,
                  type: division2.type
                }
              ]
            };
            events.push(data);
          } else if (event.division > 0) {
            continue;
          } else {
            events.push({
              event_id: eventId,
              name: event.name,
              type: event.type,
              divisions: []
            });
          }
        } catch (e) {
          console.log(e);
        }
      }
      document.querySelector('#events-list').innerHTML = '';
      for (const event of events) {
        document.getElementById('events-list').innerHTML += `<li class="mdc-list-item" id="event-${event.event_id}">
          <span class="mdc-list-item__graphic mdi mdi-calendar-outline"></span>
          <span class="mdc-list-item__text">
            ${event.divisions.length > 0 ? event.name + ' - ' + ' Dual Division' : event.name}
          </span>
        </li>`;
        document
          .querySelector('#event-' + event.event_id)
          .setAttribute('onclick', `setup.selectEvent(${JSON.stringify(event)})`);
      }
      document.querySelector('#step2-description').hidden = events.length === 0;
      if (events.length === 0) {
        document.querySelector('#events-list').innerHTML += `<div class="pt-2 text-center">
          <div class="mdc-typography--subtitle1">No events found</div>
          <div class="mdc-typography--body2">Please <a href="#" onclick="setup.openExternalLink('http://${skHost}/setup/event')">create your Scorekeeper event</a> first.</div>
          <button class="mdc-button mt-2" onclick="setup.loadScorekeeperEvents()">Retry</button>
        </div>`;
      }
    })
    .catch(console.error);
}

function selectEvent(event) {
  if (event.divisions.length > 0) {
    localStorage.setItem('CONFIG-EVENTS', JSON.stringify(event.divisions));
  } else {
    delete event.divisions;
    localStorage.setItem('CONFIG-EVENTS', JSON.stringify([event]));
  }
  // Send API Key Request
  apiKeyRetryAttempts = 0;
  const apiKey = localStorage.getItem('SCOREKEEPER-KEY');
  if (apiKey && apiKey !== '') {
    waitForKey(apiKey);
  } else {
    requestNewApiKey();
  }
}

function waitForKey(key) {
  const skHost = localStorage.getItem('SCOREKEEPER-IP');
  apis // This is a long-poll request... will not resolve until either socket is closed or key is approved
    .scorekeeperFromIp(skHost)
    .get('/v1/keywait/', { headers: { authorization: key } })
    .then((data) => {
      if (data.data.active) {
        showStep(4);
      } else {
        showSnackbar('Scorekeeper API key not accepted. Please refresh and try again.');
      }
    })
    .catch((error) => {
      requestNewApiKey();
      console.error(error);
      showSnackbar('An error has occurred, please reload the and try again.');
      log('Error approving scorekeeper API key');
    });
}

function requestNewApiKey() {
  // TODO: Raise to 4 when CORS requests are made
  if (apiKeyRetryAttempts > 0) {
    showSnackbar('Maximum API key requests reached. This is currently a known bug.');
    return;
  } else {
    apiKeyRetryAttempts++;
  }
  const skHost = localStorage.getItem('SCOREKEEPER-IP');
  const name = encodeURIComponent('TOA DataSync v' + (dataSyncVersion || '0.0.0'));
  apis
    .scorekeeperFromIp(skHost)
    .post(`/v1/keyrequest/?name=${name}`)
    .then((data) => {
      document.querySelector('#scorekeeper-approve-url').innerHTML = `http://${skHost}/manage/`;
      document.querySelector('#scorekeeper-approve-url').href = `http://${skHost}/manage/`;
      document.querySelector('#key-needing-approval').innerHTML = data.data.key;
      localStorage.setItem('SCOREKEEPER-KEY', data.data.key);
      // TODO: re-enable when CORS is fixed
      // showStep(3);
      showStep(4);
      return waitForKey();
    })
    .catch((error) => {
      console.error(error);
      showSnackbar('An error has occurred, please reload the and try again.');
      log('Error requesting scorekeeper API key');
    });
}

function showSnackbar(text) {
  const snackbar = new mdc.snackbar.MDCSnackbar(document.querySelector('.mdc-snackbar'));
  snackbar.labelText = text;
  snackbar.open();
}

function openExternalLink(url) {
  window.open(url, '_blank').focus();
}

module.exports = {
  testScorekeeperConfig,
  loadScorekeeperEvents,
  selectEvent,
  getEventsFromFirebase,
  onEventKeyChanged,
  openExternalLink,
  login
};
