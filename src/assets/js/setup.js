const apis = require('../apis');
const appbar = require('../assets/js/appbar');
const toaApi = apis.toa;
const scorekeeperApi = apis.scorekeeper;
const minScorekeeperVersion = apis.minScorekeeperVersion;

mdc.autoInit();
appbar.init();

function getEventsFromFirebase() {
  let isAdmin = false;
  let adminEvents = [];
  firebase.auth().onAuthStateChanged(async user => {
    if (user) {
      await user.getIdTokenResult(true).then(async (value) => {
        return await apis.cloud(value.token).get('/user', { headers: {
            short: true
          }}).then(me => {
          if (me.data.level >= 6) {
            isAdmin = true;
          } else {
            throw 'non-admin user';
          }
        }).catch(async () => {
          return await apis.cloud(value.token).get('/getAllCurrentWriteEvents').then(data => {
            adminEvents = data.data;
          }).catch(function (error) {
            if (error) {
              console.error(error);
              showSnackbar("An error has occurred, please restart the app and try again.");
              console.log("Error in getting cloud");
            }
          })
        });
      }).catch(function (error) {
        if (error.code && error.message) {
          console.error(error);
          showSnackbar("An error has occurred, please restart the app and try again.");
          console.log("Couldn't get token");
        }
      });
      const content = document.querySelector('#cards');
      const loading = document.querySelector('.center-spin');
      const fab = document.querySelector('.mdc-fab');
      const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
      events.forEach((event) => {
        let html = '';
        html += `<div class="mdc-card ${events[events.length - 1] !== event ? 'mb-3' : ''}">
          <div class="card__primary">
            <h2 class="card__title mdc-typography mdc-typography--headline6" style="font-size: 1rem; line-height: 1rem;">Select the TOA Event for <i>${event.name} ${events.length > 1 ? 'Division' : ''}</i></h2>
          </div>`;

        if (isAdmin) {
          html += `<div class="mdc-text-field mdc-text-field--no-label" data-mdc-auto-init="MDCTextField" data-event-input>
            <input type="text" class="mdc-text-field__input" placeholder="Event Key" oninput="onEventKeyChanged()">
            <div class="mdc-line-ripple"></div>
          </div>`;
        } else {
          html += `<div class="mdc-select event-select w-100" style="height: 52px" data-mdc-auto-init="MDCSelect"
            data-event-input data-event-id="${event.event_id}">
            <i class="mdc-select__dropdown-icon"></i>
            <div class="mdc-select__selected-text setup-event-select">Select an event...</div>
            <div class="mdc-select__menu mdc-menu mdc-menu-surface" style="width: calc(100vw - 72px)">
              <ul class="mdc-list">`;
          adminEvents.forEach((event) => {
            html += `<li class="mdc-list-item"  data-value="${event.event_key}"><span>${event.event_name}
              ${event.division_name ? `<span style="font-weight: 500"> - ${event.division_name} Division</span>` : `` }
            </span></li>`
          });
          html += `</ul>
            </div>
            <div class="mdc-line-ripple"></div>
          </div>`;
        }
        html += '</div>';
        content.innerHTML += html;
      });
      fab.hidden = false;
      loading.hidden = true;
      mdc.autoInit();
      Array.from(document.querySelectorAll('[data-mdc-auto-init="MDCSelect"]')).forEach((input) => {
        input[input.dataset.mdcAutoInit].listen('MDCSelect:change', onEventKeyChanged);
      });
    } else {
      showSnackbar("An error has occurred, please log in again.");
    }
  });
}

function selectedToaEvent(btn) {
  btn.querySelector('.mdc-fab__label').textContent = 'Loading...';
  btn.disabled = true;
  btn.onclick = null;

  const inputs = Array.from(document.querySelectorAll('[data-event-input]'));
  const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
  for (let i = 0; i < events.length; i++) {
    events[i].toa_event_key = inputs[i][inputs[i].dataset.mdcAutoInit].value
  }

  firebase.auth().onAuthStateChanged(async (user) => {
    if (user === null) {
      showSnackbar('An error has occurred. Please restart application and try again.');
    } else {
      let isValid = true;
      await user.getIdToken(true).then(async (token) => {
        for (const event of events) {
          const eventKey = event.toa_event_key;
          await apis.cloud(token).get('/getAPIKey', { headers: { data: eventKey }}).then((data) => {
            const apiKey = data.data.key;
            console.log(data.data);
            return apis.toaFromApiKey(apiKey).get('/event/' + eventKey).then(() => {
              event.toa_api_key = apiKey;
            }).catch((error) => {
              console.log(error.response);
              if (error.response.status === 403) {
                showSnackbar('Your access for this event has expired.');
              } else if (error.response.status === 404) {
                showSnackbar('Event not found.');
              } else {
                showSnackbar(error.response.data['_message'] || 'Cannot access TOA servers. Please make sure you have an Internet connection')
              }
              isValid = false;
            });
          })
        }
      }).catch(() => {
        showSnackbar('An Error has occurred. Please restart application and try again.');
        isValid = false;
      });
      if (isValid) {
        btn.querySelector('.mdc-fab__label').textContent = 'Successful!';
        localStorage.setItem('CONFIG-EVENTS', JSON.stringify(events));
        location.href = '../index.html';
      } else {
        btn.querySelector('.mdc-fab__label').textContent = 'Retry';
        onEventKeyChanged(); // Removes the disable
      }
    }
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
  const fab = document.querySelector('.mdc-fab');
  fab.disabled = !isValid;
  fab.onclick = isValid ? () => selectedToaEvent(fab) : null;
}

function testScorekeeperConfig(btn) {
  const ipAddress = document.getElementById('ip-address').MDCTextField.value;
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
    location.href = './step2.html';
  }).catch((data) => {
    console.log(data);
    btn.textContent = 'Retry';
    btn.disabled = false;
    showSnackbar(typeof data === 'string' ? data : 'Cannot access the scorekeeper.');
  });
}

function loadScorekeeperEvents() {
  const ipAddress = localStorage.getItem('SCOREKEEPER-IP');
  const eventName = localStorage.getItem('SETUP-EVENT-NAME');
  if (!ipAddress) {
    location.href = './step1.html';
    return;
  }
  document.getElementById('subtitle').innerHTML += eventName ? ` for <i>${eventName}</i>.` : '.';
  scorekeeperApi.get('/v1/events').then(async (data) => {
    const events = [];
    for (const eventId of data.data.eventCodes) {
      const event = (await scorekeeperApi.get('/v1/events/' + eventId)).data;
      if (eventId.endsWith('_0') && event.finals) {
        const baseEventId = eventId.substring(0, eventId.length - 2);
        const division1 = (await scorekeeperApi.get('/v1//events/' + baseEventId + '_1')).data;
        const division2 = (await scorekeeperApi.get('/v1/events/' + baseEventId + '_2')).data;

        let data = {
          event_id: eventId,
          name: event.name,
          divisions: [
            {
              event_id: eventId,
              name: 'Finals'
            },
            {
              event_id: division1.eventCode,
              name: division1.name
            },
            {
              event_id: division2.eventCode,
              name: division2.name
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
          divisions: []
        });
      }
    }

    for (const event of events) {
      document.getElementById('events-list').innerHTML +=
        `<li class="mdc-list-item" onclick='selectEvent(${JSON.stringify(event)})'>
          <span class="mdc-list-item__graphic mdi mdi-calendar-outline"></span>
          <span class="mdc-list-item__text">
            ${event.divisions.length > 0 ? event.name + ' - ' + ' Dual Division' : event.name}
          </span>
        </li>`;
    }
  }).catch(() => {
    location.href = './step1.html';
  });
}

function selectEvent(event) {
  if (event.divisions.length > 0) {
    localStorage.setItem('CONFIG-EVENTS', JSON.stringify(event.divisions));
  } else {
    delete event.divisions;
    localStorage.setItem('CONFIG-EVENTS', JSON.stringify([event]));
  }
  location.href = './step3.html';
}

function showSnackbar(text) {
  const snackbar = new mdc.snackbar.MDCSnackbar(document.querySelector('.mdc-snackbar'));
  snackbar.labelText = text;
  snackbar.open();
}
