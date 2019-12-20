const ui = require('./sync-ui');
const apis = require('../../apis');
const ReconnectingWebSocket = require('reconnectingwebsocket');
const uploadMatchDetails = require('./match-details');

const parser = (event) => {
  const { eventId, eventKey, apiKey, isFinalDivision } = event;
  const logger = require('./logger');
  const toaApi = apis.toa(apiKey);
  const scorekeeperApi = apis.scorekeeper;
  let lastModifiedQuals;
  ui.setStatus('loading');

  function log(...args) {
    console.log(...args);
    logger.write(...args)
  }

  scorekeeperApi.interceptors.response.use(
    (response) => !response.config || response.config.returnRes ? response : response.data,
    (error) => {
      if (error && error.response && error.response.config) {
        const res = error.response.config;
        log('SK-ERROR', error.response.statusText, res.method.toUpperCase() + ' ' + (res.url.replace(res.baseURL, '')), response.data);
      }
      return Promise.reject(error);
    }
  );

  toaApi.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error && error.response && error.response.config) {
        const res = error.response.config;
        log('TOA-ERROR', error.response.statusText, res.method.toUpperCase() + ' ' + (res.url.replace(res.baseURL, '')), JSON.parse(res.data));
      }
      return Promise.reject(error);
    }
  );

  // Start loop
  setInterval(parseAndUpload, 10 * 60 * 1000); // Every 10 minutes
  parseAndUpload();

  // Fast update matches
  const host = localStorage.getItem('SCOREKEEPER-IP').replace('http://', '');
  const socket = new ReconnectingWebSocket(`ws://${host}/api/v2/stream/?code=${eventId}`);
  socket.debug = true;
  socket.timeoutInterval = 20 * 1000;  // 20 seconds
  socket.maxReconnectInterval = 5 * 60 * 1000; // 5 minutes
  socket.onmessage = async (data) => {
    const json = JSON.parse(data.data);
    const matchName = json.payload.shortName;
    if (json.updateType === "MATCH_COMMIT") {
      log(`Fast uploading ${matchName}.`);
      if (matchName.startsWith('Q')) {
        const match = (await scorekeeperApi.get(`/v1/events/${eventId}/matches/${json.payload.number}/`)).matchBrief;
        retrieveRankings();
        return parseAndUploadMatch(match);
      } else if (matchName.startsWith('SF') || matchName.startsWith('F')) {
        const alliances = (await scorekeeperApi.get(`/v1/events/${eventId}/elim/alliances/`)).alliances;
        const elims = await fetchElimMathes();
        const index = elims.findIndex(m => m.match === matchName);
        return parseAndUploadElimMatch(index, elims, alliances);
      }
    }
  };
  socket.onopen = healthCheck;
  socket.onclose = healthCheck;

  // Health check
  healthCheck();
  window.addEventListener('online', healthCheck);
  window.addEventListener('offline', healthCheck);
  function healthCheck() {
    const socketState = socket.readyState;
    const isOnline = navigator.onLine;
    if (!isOnline) {
      ui.setStatus('no-internet');
    } else if (socketState >= 2) { // Close
      ui.setStatus('no-scorekeeper');
    } else if (socketState === 1) { // Open
      ui.setStatus('ok');
    }
  }

  function parseAndUpload() {
    retrieveMatches();
    retrieveTeams();
  }

  async function retrieveMatches() {
    const qualMatchesRes = await scorekeeperApi.get(`/v1/events/${eventId}/matches/`, {
      returnRes: true
    });
    const qualMatches = qualMatchesRes.data.matches;
    const modifiedTime = qualMatchesRes.headers['last-modified'];

    const isQualsFinished = isFinalDivision || (qualMatches.length > 0 && qualMatches.every(m => m.finished));

    ///// Qualification Match Parsing /////
    if (!lastModifiedQuals || lastModifiedQuals !== modifiedTime) {
      for (const match of qualMatches) {
        parseAndUploadMatch(match);
      }
      retrieveRankings();
      lastModifiedQuals = qualMatchesRes.headers['last-modified'];
    } else {
      log('Qual matches have not been modified.')
    }

    ///// Elimination Match Parsing /////
    if (isQualsFinished) {
      const alliances = (await scorekeeperApi.get(`/v1/events/${eventId}/elim/alliances/`)).alliances;
      const elims = await fetchElimMathes();
      for (let i = 0; i < elims.length; i++) {
        parseAndUploadElimMatch(i, elims, alliances);
      }
    }
  }

  async function parseAndUploadElimMatch(index, elims, alliances) {
    const match = elims[index];
    const matchName = match.match;

    if (matchName.startsWith('SF')) {
      return parseAndUploadMatch(match, index + 1, matchName.substr(2,1), 30, alliances);
    } else if (matchName.startsWith('F') || matchName.startsWith('IF')) {
      return parseAndUploadMatch(match, index + 1, 0, 4, alliances);
    }
  }

  async function parseAndUploadMatch(match, numberElimMatchesPlayed, elimNumber, tournLevel, alliances) {
    const participants = [];
    const isQual = !tournLevel || tournLevel === 1;
    const elimMatchNumber = isQual ? -1 : match.match.toString().split('-')[1];
    const matchNumber = isQual ? match.matchNumber.toString() : numberElimMatchesPlayed;
    let details;
    if (isQual) {
      details = await scorekeeperApi.get(`/2020/v1/events/${eventId}/matches/${matchNumber}/`);
      if (details && details.scheduledTime) {
        ui.setScheduleAccess(details.scheduledTime > 0);
      }
    } else {
      elimNumber = parseInt(elimNumber);
      details = await scorekeeperApi.get(`/2020/v1/events/${eventId}/elim/${(tournLevel === 30) ? `sf/${elimNumber}` : 'finals'}/${elimMatchNumber}/`);
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
      tournament_level: tournLevel && tournLevel === 30 ? (tournLevel + elimNumber) : (isQual ? 1 : tournLevel),
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

    log('Uploading match data for ' + matchKey, matchJSON);
    toaApi.put(`/event/${eventKey}/matches/${matchKey}`, JSON.stringify([matchJSON]));
    toaApi.put(`/event/${eventKey}/matches/${matchKey}/participants`, JSON.stringify(participants));
    uploadMatchDetails(details, matchKey, eventKey);
  }

  async function fetchElimMathes() {
    const getElimData = (elimType, elimNumber) =>
      scorekeeperApi.get(`/v1/events/${eventId}/elim/${elimType}/${elimNumber ? elimNumber + '/' : ''}`)
      .then(data => data.matchList)
      .catch(e => []);

    const sf1 = await getElimData('sf', 1);
    const sf2 = await getElimData('sf',2);
    const finals = await getElimData('finals');
    return [...sf1, ...sf2, ...finals];
  }

  async function retrieveTeams() {
    const cacheKey = `${eventId}-teams`;
    const result = [];
    const teams = (await scorekeeperApi.get(`/v1/events/${eventId}/teams/`)).teamNumbers;
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

    return toaApi.delete(`/event/${eventKey}/teams`).finally(() =>
      toaApi.post(`/event/${eventKey}/teams`, JSON.stringify(result))
    );
  }

  async function retrieveRankings() {
    const cacheKey = `${eventId}-rankings`;
    const rankings = (await scorekeeperApi.get(`/v1/events/${eventId}/rankings/`)).rankingList;

    if ((localStorage.getItem(cacheKey) || []) === JSON.stringify(rankings)) {
      return;
    } else {
      localStorage.setItem(cacheKey, JSON.stringify(rankings));
    }

    const result = [];
    const ranked = rankings.filter(r => r.ranking > 0);
    for (const rank of rankings) {
      if (ranked.length > 0) {
        const teamKey = rank.team.toString();
        result.push({
          rank_key: `${eventKey}-R${teamKey}`,
          event_key: eventKey,
          team_key: teamKey,
          rank: rank.ranking > 0 ? rank.ranking : 0,
          rank_change: 0,
          wins: rank.wins,
          losses: rank.losses,
          ties: rank.ties,
          highest_qual_score: rank.highestScore,
          ranking_points: rank.rankingPoints && rank.rankingPoints !== '--' ? parseFloat(rank.rankingPoints) || 0 : 0,
          qualifying_points: 0,
          tie_breaker_points: rank.tieBreakerPoints && rank.tieBreakerPoints !== '--' ? parseFloat(rank.tieBreakerPoints) || 0 : 0,
          disqualified: 0,
          played: rank.matchesPlayed
        });
      }
    }

    log('Uploading rankings...', result);
    return toaApi.delete(`/event/${eventKey}/rankings`).finally(() =>
      toaApi.post(`/event/${eventKey}/rankings`, JSON.stringify(result))
    );
  }
};

function generateMatchName(tournLevel, matchNumber, elimNumber){
  if (tournLevel === 30) {
    return `Semis ${elimNumber} Match ${matchNumber}`
  } else if (tournLevel === 4) {
    return `Finals ${matchNumber}`
  } else {
    return null;
  }
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

module.exports = { parser, ui };
