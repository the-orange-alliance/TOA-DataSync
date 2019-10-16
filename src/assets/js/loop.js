const ui = require('./assets/js/sync-ui');
const apis = require('./apis');
const logger = require('./assets/js/logger');
const WebSocket = require('ws');
const toaApi = apis.toa;
const scorekeeperApi = apis.scorekeeper;
const uploadMatchDetails = require('./assets/js/match-details');
const index = parseInt(new URLSearchParams(window.location.search).get('i'), 10);
const configEvent = JSON.parse(localStorage.getItem('CONFIG-EVENTS'))[index];
const eventId = configEvent.event_id;
const eventKey = configEvent.toa_event_key;
let scorekeeperWorks = false;
ui.setStatus('loading');

function log(...args) {
  console.log(...args);
  logger.write(...args)
}

scorekeeperApi.interceptors.response.use(
  response => {
    scorekeeperWorks = true;
    return response.data
  },
  (error) => {
    const status = error && error.response && error.response.status ? error.response.status : 0;
    if (status !== 500) {
      scorekeeperWorks = false;
      ui.setStatus('no-scorekeeper');
    }
    return Promise.reject(error);
  }
);

toaApi.interceptors.response.use(
  response => response,
  (error) => {
    const status = error && error.response && error.response.status ? error.response.status : 0;
    if (status !== 500) {
      const res = error.response.config;
      log(error.response.statusText, res.method.toUpperCase() + ' ' + (res.url.replace(res.baseURL, '')), JSON.parse(res.data));
    }
    return Promise.reject(error);
  }
);

// Start loop
setInterval(retrieveTeams, 10 * 1000 * 60); // Every 10 minutes
retrieveTeams();
retrieveMatches(); // Upload old data
retrieveRankings();

const host = localStorage.getItem('SCOREKEEPER-IP').replace('http://', '');
const socket = new WebSocket(`ws://${host}/api/v2/stream/?code=${eventId}`);
socket.on('message', async (data) => {
  const json = JSON.parse(data);
  console.log(`[WS] ${json.updateType}`, json);
  const matchName = json.payload.shortName;
  if (json.updateType === "MATCH_COMMIT") {
    if (matchName.startsWith('Q')) {
      const match = (await scorekeeperApi.get(`/v1/events/${eventId}/matches/${json.payload.number}`)).matchBrief;
      parseAndUploadMatch(match);
    } else if (matchName.startsWith('SF') || matchName.startsWith('F')) {
      const alliances = (await scorekeeperApi.get(`/v1/events/${eventId}/elim/alliances`)).alliances;
      const elims = await fetchElimMathes();
      const match = elims.find(m => m.match === matchName);
      const index = elims.indexOf(match) + 1;
      console.log(match, index);

      if (matchName.startsWith('SF')) {
        parseAndUploadMatch(match, index, matchName.substr(2,1), 30, alliances);
      } else if (matchName.startsWith('F')) {
        parseAndUploadMatch(match, index, 0, 4, alliances);
      }
    }
  }
});

async function retrieveMatches() {
  /////////////// Qualification Match Parsing ///////////////
  const isFinalDivision = index === 0 && JSON.parse(localStorage.getItem('CONFIG-EVENTS')).length > 1;
  const qualMatches = (await scorekeeperApi.get(`/v1/events/${eventId}/matches/`)).matches;
  const isQualsFinished = isFinalDivision || (qualMatches.length > 0 && qualMatches.every(m => m.finished));

  for (const match of qualMatches) {
    parseAndUploadMatch(match);
  }

  if (isQualsFinished) {
    scorekeeperApi.get(`/v1/events/${eventId}/elim/alliances`).then(async (data) => {
      const alliances = data.alliances;
      const sf1Matches = await getElimDataFromSK(1, 'sf');
      const sf2Matches = await getElimDataFromSK(2, 'sf');

      let numberElimMatchesPlayed = 1;

      // Note: shift() returns the first element of the array and then removes it

      /////////////// Semifinals Match Parsing ///////////////
      while (sf1Matches && sf1Matches.length > 0) {
        parseAndUploadMatch(sf1Matches.shift(), numberElimMatchesPlayed++, 1, 30, alliances);
      }
      while (sf2Matches && sf2Matches.length > 0) {
        parseAndUploadMatch(sf2Matches.shift(), numberElimMatchesPlayed++, 2, 30, alliances);
      }

      /////////////// Finals Match Parsing ///////////////
      const finalsMatches = await getElimDataFromSK('', 'finals');
      for (const finalsMatch of (finalsMatches || [])) {
        parseAndUploadMatch(finalsMatch, numberElimMatchesPlayed++, 0, 4, alliances);
      }
    });
  }
}

function getElimDataFromSK(elimNumber, elimType) {
  return scorekeeperApi.get(`/v1/events/${eventId}/elim/${elimType}/${elimNumber}`)
    .then(data => data.matchList)
    .catch(e => []);
}

async function parseAndUploadMatch(match, numberElimMatchesPlayed, elimNumber, tournLevel, alliances){
  const participants = [];
  const isQual = !tournLevel || tournLevel === 1;
  const elimMatchNumber = isQual ? -1 : match.match.toString().split('-')[1];
  const matchNumber = isQual ? match.matchNumber.toString() : numberElimMatchesPlayed;
  let details;
  if (isQual) {
    details = await scorekeeperApi.get(`/2020/v1/events/${eventId}/matches/${matchNumber}`);
    if (details && details.scheduledTime) {
      ui.setScheduleAccess(details.scheduledTime > 0);
    }
  } else {
    details = await scorekeeperApi.get(`/2020/v1/events/${eventId}/elim/${(tournLevel === 30) ? `sf/${elimNumber}` : 'finals'}/${elimMatchNumber}`);
  }
  const hasDetails = details && details.matchBrief && details.matchBrief.matchState === 'COMMITTED';

  // Format Match Number
  let matchCode = '';
  if (matchNumber < 10) {
    matchCode = '00' + matchNumber;
  } else if (matchNumber < 99) {
    matchCode = '0' + matchNumber;
  } else {
    matchCode = matchNumber;
  }

  const matchKey = `${eventKey}-${isQual ? 'Q' : 'E'}${matchCode}-1`;

  const getDateString = (unix) => new Date(unix).toISOString().split('.')[0];

  let matchJSON = {
    match_key: matchKey,
    event_key: eventKey,
    tournament_level: tournLevel && tournLevel === 30 ? tournLevel + elimNumber : (isQual ? 1 : tournLevel),
    scheduled_time: details && details.scheduledTime > 0 ? getDateString(details.scheduledTime) : null,
    match_start_time: details && details.startTime > 0 ? getDateString(details.startTime) : null,
    match_name: isQual ? `Quals ${matchNumber}` : generateMatchName(tournLevel, elimMatchNumber, elimNumber),
    play_number: 1,
    field_number: hasDetails ? details.matchBrief.field : -1,
    red_score: hasDetails ? details.redScore : -1,
    blue_score: hasDetails ? details.blueScore : -1,
    red_penalty: hasDetails ? details.red.penalty : -1,
    blue_penalty: hasDetails ? details.blue.penalty : -1,
    red_auto_score: hasDetails ? details.red.auto : -1,
    blue_auto_score: hasDetails ? details.blue.auto : -1,
    red_tele_score: hasDetails ? details.red.teleop : -1,
    blue_tele_score: hasDetails ? details.blue.teleop : -1,
    red_end_score: hasDetails ? details.red.end : -1,
    blue_end_score: hasDetails ? details.blue.end : -1,

    last_commit_time: details && details.matchBrief.time > 0 ? details.matchBrief.time : -1 // local using only
  };

  const clearMatchJSON = () => {
    const json = JSON.parse(JSON.stringify(matchJSON));
    delete json.last_commit_time;
    delete json.participants;
    return json;
  };

  if (isQual) {
    participants.push(buildParticipant(match.red.team1, 'R1', matchKey, match.red.isTeam1Surrogate));
    participants.push(buildParticipant(match.red.team2, 'R2', matchKey, match.red.isTeam2Surrogate));
    participants.push(buildParticipant(match.blue.team1, 'B1', matchKey, match.blue.isTeam1Surrogate));
    participants.push(buildParticipant(match.blue.team2, 'B2', matchKey, match.blue.isTeam2Surrogate));
  } else if (alliances && alliances.length > 0) {
    const red = alliances.filter(a => a.seed === match.red.seed)[0];
    const blue = alliances.filter(a => a.seed === match.blue.seed)[0];

    participants.push(buildParticipant(red.captain, 'R1', matchKey, match.red.captain === -1));
    participants.push(buildParticipant(red.pick1, 'R2', matchKey, match.red.pick1 === -1));
    if (red.pick2 !== -1 && blue.pick2 !== -1) {
      participants.push(buildParticipant(red.pick2, 'R3', matchKey, match.red.pick2 === -1));
    }

    participants.push(buildParticipant(blue.captain, 'B1', matchKey, match.blue.captain === -1));
    participants.push(buildParticipant(blue.pick1, 'B2', matchKey, match.blue.pick1 === -1));
    if (red.pick2 !== -1 && blue.pick2 !== -1) {
      participants.push(buildParticipant(blue.pick2, 'B3', matchKey, match.blue.pick2 === -1));
    }
  }

  if (participants.length <= 0) return;

  const shortMatchKey = matchKey.split('-')[3];
  let oldMatch = localStorage.getItem(`${eventId}-match-${shortMatchKey}`);

  // Regenerated schedule
  if (oldMatch && JSON.stringify(participants) !== JSON.stringify(JSON.parse(oldMatch).participants)) {
    log('UPDATING the participants of ' + matchKey);
    await toaApi.put(`/event/${eventKey}/matches/${matchKey}/participants`, JSON.stringify(participants));
    matchJSON.participants = participants;
    localStorage.setItem(`${eventId}-match-${shortMatchKey}`, JSON.stringify(matchJSON));
    delete matchJSON.participants;
  }

  // Score Update (Last Commit Time != current commit time
  if (oldMatch && JSON.parse(oldMatch).last_commit_time !== matchJSON.last_commit_time) {

    log('UPDATING match data from ' + matchKey);
    await toaApi.put(`/event/${eventKey}/matches/${matchKey}`, JSON.stringify([clearMatchJSON()]));
    matchJSON.participants = participants;
    localStorage.setItem(`${eventId}-match-${shortMatchKey}`, JSON.stringify(matchJSON));
    delete matchJSON.participants;

    // Update match details
    uploadMatchDetails(details, matchKey, eventKey, true);

  } else if (!oldMatch) {
    // Not Uploaded Yet, Nothing in the localStorage
    log('UPLOADING match data and participants from ' + matchKey);
    // Upload Participants
    await toaApi.post(`/event/${eventKey}/matches/participants`, JSON.stringify(participants)).catch(() => {});

    // Upload match details
    uploadMatchDetails(details, matchKey, eventKey, false).catch(() => {});

    // Upload Match Data
    await toaApi.post(`/event/${eventKey}/matches`, JSON.stringify([clearMatchJSON()])).then(() => {
      matchJSON.participants = participants;
      localStorage.setItem(`${eventId}-match-${shortMatchKey}`, JSON.stringify(matchJSON));
      delete matchJSON.participants;
    }).catch(async (data) => {
      if (data.response.status === 500) {
        // The match is already on TOA. This SHOULD never happen, but it's a good catch to have.
        log('Match already on TOA. UPDATING localStorage for ' + matchKey);
        const toaMatch = (await toaApi.get(`/match/${matchKey}`)).data[0];
        toaMatch.participants = (await toaApi.get(`/match/${matchKey}/participants`)).data;
        localStorage.setItem(`${eventId}-match-${shortMatchKey}`, JSON.stringify(toaMatch));
      }
    });
  }
}

function generateMatchName(tournLevel, matchNumber, elimNumber){
  if (tournLevel === 30) {
    return `Semis ${elimNumber} Match ${matchNumber}`
  } else if (tournLevel === 40) {
    return `Finals Match ${matchNumber}`
  } else {
    return null;
  }
}

async function fetchElimMathes() {
  const sf1 = await getElimDataFromSK(1, 'sf');
  const sf2 = await getElimDataFromSK(2, 'sf');
  const finals = await getElimDataFromSK('', 'finals');
  return [...sf1, ...sf2, ...finals];
}

async function retrieveTeams() {
  const cacheKey = `${eventId}-teams`;
  const result = [];
  const teams = (await scorekeeperApi.get(`/v1/events/${eventId}/teams`)).teamNumbers;
  if (JSON.stringify(teams) === localStorage.getItem(cacheKey)) {
    return;
  } else {
    localStorage.setItem(cacheKey, JSON.stringify(teams));
  }

  for (let i = 0; i < teams.length; i++) {
    result.push({
      event_key: eventKey,
      event_participant_key: `${eventKey}-T${i}`,
      team_key: teams[i].toString(),
      is_active: true,
      card_status: null
    });
  }

  await toaApi.delete(`/event/${eventKey}/teams`);
  await toaApi.post(`/event/${eventKey}/teams`, JSON.stringify(result));
}

async function retrieveRankings() {
  const cacheKey = `${eventId}-rankings`;
  const rankings = (await scorekeeperApi.get(`/v1/events/${eventId}/rankings`)).rankingList;

  if ((localStorage.getItem(cacheKey) || []) === JSON.stringify(rankings)) {
    return
  } else {
    localStorage.setItem(cacheKey, JSON.stringify(rankings));
  }

  const result = [];
  for (const rank of rankings) {
    if (rank.ranking > 0) {
      const teamKey = rank.team.toString();
      result.push({
        rank_key: `${eventKey}-R${teamKey}`,
        event_key: eventKey,
        team_key: teamKey,
        rank: rank.ranking,
        rank_change: 0,
        wins: 0,
        losses: 0,
        ties: 0,
        highest_qual_score: 0,
        ranking_points: rank.rankingPoints,
        qualifying_points: 0,
        tie_breaker_points: rank.tieBreakerPoints && rank.tieBreakerPoints !== '--' ? rank.tieBreakerPoints : 0,
        disqualified: 0,
        played: rank.matchesPlayed
      });
    }
  }

  log('Uploading rankings...', result);
  toaApi.delete(`/event/${eventKey}/rankings`).then(() => {
    return toaApi.post(`/event/${eventKey}/rankings`, JSON.stringify(result));
  });
}

function buildParticipant(teamKey, suffix, matchKey, isSurrogate, noShow = false) {
  teamKey = teamKey.toString();
  return {
    match_participant_key: `${matchKey}-${suffix}`,
    match_key: matchKey,
    team_key: teamKey,
    station: getStation(suffix),
    station_status: isSurrogate ? 0 : (noShow) ? 2 : 1, // 0 = Surrogate, 1 = Normal, 2 = No Show/Sit Out
    ref_status: 0
  };
}

function getStation(suffix) {
  switch (suffix) {
    case 'R1':
      return 11;
    case 'R2':
      return 12;
    case 'R3':
      return 13;
    case 'B1':
      return 21;
    case 'B2':
      return 22;
    case 'B3':
      return 23;
    default:
      throw Error(`Invalid station suffix: "${suffix}"`);
  }
}
