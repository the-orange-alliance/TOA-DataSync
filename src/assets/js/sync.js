const { parser, ui} = require('./parser');
const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));

for (let i = 0; i < events.length; i++) {
  const event = events[i];
  const eventId = event.event_id;
  const eventKey = event.toa_event_key;
  const apiKey = event.toa_api_key;
  const type = event.type;
  const isFinalDivision = i === 0 && events.length > 1;
  parser({ eventId, eventKey, apiKey, isFinalDivision, type });
}

module.exports = ui;
