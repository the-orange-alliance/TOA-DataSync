const ui = require('./assets/js/sync-ui');
const apis = require('./apis');
const logger = require('./assets/js/logger');
const toaApi = apis.toa;
const scorekeeperApi = apis.scorekeeper;
const uploadMatchDetails = require('./assets/js/match-details');
const index = parseInt(new URLSearchParams(window.location.search).get('i'), 10);
const configEvent = JSON.parse(localStorage.getItem('CONFIG-EVENTS'))[index];
const eventId = configEvent.event_id;
const eventKey = configEvent.toa_event_key;
let scorekeeperWorks = false;
let eventStatus = localStorage.getItem(`${eventId}-eventStatus`) || 0;
ui.setStatus('loading');

setInterval(start, 30 * 1000);
start();

function updateEventStatus(newStatus) {
  /*
   * 0 - Setup
   * 1 - Teams Entered
   * 2 - Qual Matches in Progress
   * 3 - SF Matches in Progress
   * 4 - Finals Matches in Progress
   * 5 - Awards Available
   */
  eventStatus = newStatus;
  localStorage.setItem(`${eventId}-eventStatus`, eventStatus);
  log('Event Status: ' + eventStatus);
  // TODO Update UI to present new status
}

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
    const res = error.response.config;
    log(error.response.statusText, res.method.toUpperCase() + ' ' + (res.url.replace(res.baseURL, '')), JSON.parse(res.data));
  }
);

async function start() {
  if (ui.getShouldUpload()) {
    // Check the connection to TOA servers
    const dns = require('dns').promises;
    dns.lookup('theorangealliance.org').then(() => {
      if (scorekeeperWorks) {
        ui.setStatus('ok');
      }
      Promise.all([
        retrieveMatches(),
        retrieveRankings(),
        retrieveTeams()
      ]);
      return; // DO NOT return the Promise.all
    }).catch(() => {
      ui.setStatus('no-internet');
    });
  }
}

function clearCache() {
  log('Clearing cache...');
  for (const key in localStorage) {
    if (key.startsWith(`${eventId}-`)) {
      localStorage.removeItem(key);
    }
  }
  console.log('Done!');
}

async function retrieveMatches() {
  // Cache Keys
  const cacheKeyQual = `${eventId}-matches-qual`;
  const cacheKeySF1 = `${eventId}-matches-SF1`;
  const cacheKeySF2 = `${eventId}-matches-SF2`;
  const cacheKeyFinal = `${eventId}-matches-finals`;

  /////////////// Qualification Match Parsing ///////////////
  // Contact Scorekeeping System for Qual Data
  let data = await scorekeeperApi.get(`/v1/events/${eventId}/matches/`).catch(e => {
    return  scorekeeperApi.get(`/v1/events/${eventId}/matches/`); // FIRST-Tech-Challenge/scorekeeper#367
  });

  if (!data) {
    eventStatus = 0;
    return;
  }

  const qualMatches = data.matches;

  if ((localStorage.getItem(cacheKeyQual) || []) !== JSON.stringify(qualMatches)) {
    // return; // Match data is the same as we last checked, no need to continue
  } else {
    localStorage.setItem(cacheKeyQual, JSON.stringify(qualMatches)); // Match Data is new, update and continue parsing
  }

  const isFinalDivision = index === 0 && JSON.parse(localStorage.getItem('CONFIG-EVENTS')).length > 1;
  let allQualMatchesFinished = isFinalDivision || qualMatches.length > 0;

  for (const match of qualMatches) {

    // Check if match is finished
    if (!match.finished) {
      allQualMatchesFinished = false; // If match is not finished, then not all qual matches are finished.
    }

    parseAndUploadMatch(match);
  }

  /* Elims Are Madness
   * At TOA we number Elims based in the order in which they are played. For instance:
   * Elim 1 - SF1-1
   * Elim 2 - SF2-1
   * Elim 3 - SF1-2
   * Elim 4 - SF2-2
   * Elim 5 - SF2-3
   * Elim 6 - F-1
   * Elim 7 - F-2
   *
   * In order to do this PROPERLY, and not have to iterate through arrays later (like in the OLD datasync)
   * We will Get both SF1 Data and SF2 at the same time
   * Then, we will go through the first entry of the SF1 Array, then delete it from the array.
   * Next, we will go through the first entry of the SF2 Array, then delete it from the array.
   * Then, we will return to the SF1 Array and continue the pattern until both arrays are empty.
   *
   * Finally, we will check for Finals matches because we have no idea when all of the SF matches are finished.
   *
   * God, I hope this works
   * *************/
  let numberElimMatchesPlayed = 1;

  if (allQualMatchesFinished) {
    scorekeeperApi.get(`/v1/events/${eventId}/elim/alliances`).then(async (data) => {
      const alliances = data.alliances;
      const sf1Matches = await getElimDataFromSK(cacheKeySF1, 1, 'sf');
      const sf2Matches = await getElimDataFromSK(cacheKeySF2, 2, 'sf');

      // Note: shift() returns the first element of the array and then removes it

      /////////////// Semifinals Match Parsing ///////////////
      while (sf1Matches && sf1Matches.length > 0 ) {
        // Parse [0] SF1 Object
        parseAndUploadMatch(sf1Matches.shift(), numberElimMatchesPlayed++, 1, 30, alliances);
      }
      while (sf2Matches && sf2Matches.length > 0 ) {
        // Parse [0] SF1 Object
        parseAndUploadMatch(sf2Matches.shift(), numberElimMatchesPlayed++, 2, 30, alliances);
      }

      /////////////// Finals Match Parsing ///////////////
      const finalsMatches = await getElimDataFromSK(cacheKeyFinal, '', 'finals');
      for (const finalsMatch of (finalsMatches || [])) {
        parseAndUploadMatch(finalsMatch, numberElimMatchesPlayed++, 0, 4, alliances);
      }
    });
  }
}

async function getElimDataFromSK(cacheKey, elimNumber, elimType) {
  const rawElimData = await scorekeeperApi.get(`/v1/events/${eventId}/elim/${elimType}/${elimNumber}`);
  if (!rawElimData) {
    return;
  }

  // Set Event Status
  if (eventStatus < 3) {
    updateEventStatus(3); // If event is less than 3, set it to 3. That way we prevent back-tracking from F and awards
  }

  const elimMatches = rawElimData.matchList;

  if ((localStorage.getItem(cacheKey) || []) === JSON.stringify(elimMatches)) {
    // return; // Match data is the same as we last checked, no need to continue
  } else {
    localStorage.setItem(cacheKey, JSON.stringify(elimMatches)); // Match Data is new, update and continue parsing
  }
  return elimMatches
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

  if (participants.length <= 0) {
    return;
  }

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

   //Score Update (Last Commit Time != current commit time
  if (oldMatch && JSON.parse(oldMatch).last_commit_time !== matchJSON.last_commit_time) {
    oldMatch = JSON.parse(oldMatch);

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
    await uploadMatchDetails(details, matchKey, eventKey, false).catch(() => {});

    // Upload Match Data
    await toaApi.post(`/event/${eventKey}/matches`, JSON.stringify([clearMatchJSON()])).then(() => {
      calculateWLT(matchJSON, participants);
      matchJSON.participants = participants;
      localStorage.setItem(`${eventId}-match-${shortMatchKey}`, JSON.stringify(matchJSON));
      delete matchJSON.participants;

      // Update the event status
      if (tournLevel && tournLevel === 4 && eventStatus < 4) {
        updateEventStatus(4);
      } else if (tournLevel && tournLevel === 30 && eventStatus < 3) {
        updateEventStatus(3);
      } else if (isQual && eventStatus < 2) {
        updateEventStatus(2);
      }
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

async function retrieveTeams() {
  const cacheKey = `${eventId}-teams`;
  const result = [];
  const teams = (await scorekeeperApi.get(`/v1/events/${eventId}/teams`)).teamNumbers;
  if (`[${localStorage.getItem(cacheKey) || []}]` === JSON.stringify(teams)) {
    return
  } else {
    localStorage.setItem(cacheKey, teams);
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
      const wlt = JSON.parse(localStorage.getItem(`${eventId}-wlt-${teamKey}`) || '{}');
      result.push({
        rank_key: `${eventKey}-R${teamKey}`,
        event_key: eventKey,
        team_key: teamKey,
        rank: rank.ranking,
        rank_change: 0,
        wins: wlt && wlt.wins ? wlt.wins : 0,
        losses: wlt && wlt.loss ? wlt.loss : 0,
        ties: wlt && wlt.ties ? wlt.ties : 0,
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

async function deleteMatchesData() {
  ui.setShouldUpload(false);
  for (const key in localStorage) {
    if (key.startsWith(`${eventId}-match-`) || key.startsWith(`${eventId}-wlt-`) || key === `${eventId}-matches`) {
      localStorage.removeItem(key);
    }
  }

  await toaApi.delete(`/event/${eventKey}/matches/all`);

  log('Done');
}

function calculateWLT(match, participants) {
  const redWin = match.red_score > match.blue_score;
  const tie = match.red_score === match.blue_score;

  if (match.tournament_level !== 1 || match.red_score === -1 || match.blue_score === -1) {
    return;
  }
  for (const participant of participants) {
    const cacheKey = `${eventId}-wlt-${participant.team_key}`;
    if (participant.station_status >= 1) {
      const WINS = 'wins';
      const LOSS = 'loss';
      const TIES = 'ties';
      let result = localStorage.getItem(cacheKey);
      if (result) {
        result = JSON.parse(result);
        if (tie) {
          result[TIES]++;
        } else if (redWin && participant.station < 20) {
          // Red win and this team is on red
          result[WINS]++;
        } else if (!redWin && participant.station > 20) {
          // Blue win and this team is on blue
          result[WINS]++;
        } else {
          result[LOSS]++;
        }
      } else {
        result = {};
        if (tie) {
          result[WINS] = 0;
          result[LOSS] = 0;
          result[TIES] = 1;
        } else if (redWin && participant.station < 20) {
          // Red win and this team is on red
          result[WINS] = 1;
          result[LOSS] = 0;
          result[TIES] = 0;
        } else if (!redWin && participant.station > 20) {
          // Blue win and this team is on blue
          result[WINS] = 1;
          result[LOSS] = 0;
          result[TIES] = 0;
        } else {
          result[WINS] = 0;
          result[LOSS] = 1;
          result[TIES] = 0;
        }
      }
      localStorage.setItem(cacheKey, JSON.stringify(result));
    }
  }
}
