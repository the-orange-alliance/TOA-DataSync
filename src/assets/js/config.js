const index = parseInt(new URLSearchParams(window.location.search).get('i'), 10);
const configEvent = JSON.parse(localStorage.getItem('CONFIG-EVENTS'))[index];
const eventId = configEvent.event_id;
const eventKey = configEvent.toa_event_key;
const isFinalDivision = index === 0 && JSON.parse(localStorage.getItem('CONFIG-EVENTS')).length > 1;

module.exports = { eventId, eventKey, isFinalDivision };
