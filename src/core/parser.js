const ui = require('./sync-ui');
const apis = require('./apis');
const ReconnectingWebSocket = require('reconnectingwebsocket');
const uploadMatchDetails = require('./match-details');

const parser = (event) => {
  const { eventId, eventKey, apiKey, isFinalDivision, type } = event;
  const logger = require('./logger');
  const toaApi = apis.toa(apiKey);
  const scorekeeperApi = apis.scorekeeper;
  ui.setStatus('loading');

  function log(...args) {
    console.log(...args);
    logger.write(...args);
  }

  scorekeeperApi.interceptors.response.use(
    (response) => (!response.config || response.config.returnRes ? response : response.data),
    (error) => {
      if (error && error.response && error.response.config) {
        const res = error.response.config;
        log(
          'SK-ERROR',
          error.response.statusText,
          res.method.toUpperCase() + ' ' + res.url.replace(res.baseURL, ''),
          response.data
        );
      }
      return Promise.reject(error);
    }
  );

  toaApi.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error && error.response && error.response.config) {
        const res = error.response.config;
        log(
          'TOA-ERROR',
          error.response.statusText,
          res.method.toUpperCase() + ' ' + res.url.replace(res.baseURL, ''),
          JSON.parse(res.data)
        );
      }
      return Promise.reject(error);
    }
  );

  // Fast update matches
  const host = localStorage.getItem('SCOREKEEPER-IP').replace('http://', '');
  const socket = new ReconnectingWebSocket(`ws://${host}/api/v2/stream/?code=${eventId}`);
  socket.debug = true;
  socket.timeoutInterval = 20 * 1000; // 20 seconds
  socket.maxReconnectInterval = 5 * 60 * 1000; // 5 minutes
  socket.onmessage = async (data) => {
    const json = JSON.parse(data.data);
    const matchName = json.payload.shortName;
    if (json.updateType === 'MATCH_COMMIT') {
      log(`Fast uploading ${matchName}.`);
      if (matchName.startsWith('Q')) {
        const match = await scorekeeperApi.get(
          `/${apis.config.scorekeeperSeason}/v1/events/${eventId}/matches/${json.payload.number}/`
        );
        parseAndUploadMatch(match);

        // Upload rankings
        const isLeagueTournament = type && type === 'League Tournament';
        const rankings = (
          await scorekeeperApi.get(`/v1/events/${eventId}/rankings/${isLeagueTournament ? 'combined/' : ''}`)
        ).rankingList;
        retrieveRankings(rankings);
      } else if (matchName.startsWith('SF') || matchName.startsWith('F') || matchName.startsWith('IF')) {
        const alliances = (await scorekeeperApi.get(`/v1/events/${eventId}/elim/alliances/`)).alliances;
        const elims = (await scorekeeperApi.get(`/v1/events/${eventId}/elim/all/`)).matchList;
        let match = null;
        if (matchName.startsWith('SF')) {
          match = await scorekeeperApi.get(
            `/${apis.config.scorekeeperSeason}/v1/events/${eventId}/elim/sf/${matchName.substr(
              2,
              1
            )}/${matchName.substr(4, 1)}/`
          );
        } else {
          match = await scorekeeperApi.get(
            `/${apis.config.scorekeeperSeason}/v1/events/${eventId}/elim/finals/${matchName.substr(-1)}/`
          );
        }
        const index = elims.findIndex((m) => m.match === matchName);
        const arr = [];
        arr[index] = match;
        return parseAndUploadElimMatch(index, arr, alliances);
      }
    }
  };
  socket.onopen = healthCheck;
  socket.onclose = healthCheck;
  socket.onerror = healthCheck;

  // Health check
  healthCheck();
  window.addEventListener('online', healthCheck);
  window.addEventListener('offline', healthCheck);
  function healthCheck() {
    const socketState = socket.readyState;
    const isOnline = navigator.onLine;
    if (!isOnline) {
      ui.setStatus('no-internet');
    } else if (socketState === WebSocket.OPEN) {
      ui.setStatus('ok');
    } else if (
      socketState === WebSocket.CLOSED ||
      (socket.reconnectAttempts >= 2 && socketState === WebSocket.CONNECTING)
    ) {
      ui.setStatus('no-scorekeeper');
    }
  }

  const uploadAllData = async () => {
    log('Fetching all data...');
    const data = await scorekeeperApi.get(`/v2/events/${eventId}/full/`);
    const detailsReq = await scorekeeperApi.get(`/${apis.config.scorekeeperSeason}/v1/events/${eventId}/full/`);
    const details = detailsReq.data;
    const {
      version,
      event,
      teamList,
      rankingsList,
      combinedRankingsList,
      matchList,
      elimsMatchList,
      elimsMatchDetailedList,
      allianceList
    } = data.data;

    // Teams
    retrieveTeams(teamList.teams);

    // Rankings
    retrieveRankings(event.type === 'League Tournament' ? combinedRankingsList.rankingList : rankingsList.rankingList);

    // Qual Matches
    for (const match of matchList.matches) {
      const detail = details.matchList.matches.find((m) => m.matchBrief.matchName === match.matchBrief.matchName);
      parseAndUploadMatch(detail);
    }

    // Playoff Matches
    for (let i = 0; i < details.elimsMatchList.matches.length; i++) {
      parseAndUploadElimMatch(i, details.elimsMatchList.matches, allianceList.alliances);
    }
  };
  uploadAllData();
  setInterval(uploadAllData, 15 * 60 * 1000); // Every 15 minutes

  async function parseAndUploadElimMatch(index, elims, alliances) {
    const match = elims[index];
    const matchName = match.matchBrief.matchName;

    if (matchName.startsWith('SF')) {
      return parseAndUploadMatch(match, index + 1, parseInt(matchName.substr(2, 1)), 30, alliances);
    } else if (matchName.startsWith('F') || matchName.startsWith('IF')) {
      return parseAndUploadMatch(match, index + 1, 0, 4, alliances);
    }
  }

  async function parseAndUploadMatch(match, numberElimMatchesPlayed, elimNumber, tournLevel, alliances) {
    const participants = [];
    const isQual = !tournLevel || tournLevel === 1;
    const elimMatchNumber = isQual ? -1 : match.matchBrief.matchName.toString().split('-')[1];
    const matchNumber = isQual ? match.matchBrief.matchNumber.toString() : numberElimMatchesPlayed;
    const hasDetails = match.matchBrief.finished;

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
      tournament_level: tournLevel && tournLevel === 30 ? tournLevel + elimNumber : isQual ? 1 : tournLevel,
      scheduled_time: match.scheduledTime > 0 ? getDateString(match.scheduledTime) : null,
      match_start_time: match.startTime > 0 ? getDateString(match.startTime) : null,
      match_name: isQual ? `Quals ${matchNumber}` : generateMatchName(tournLevel, elimMatchNumber, elimNumber),
      play_number: 1,
      field_number: hasDetails ? match.matchBrief.field : -1,
      red_score: hasDetails ? match.redScore : -1,
      blue_score: hasDetails ? match.blueScore : -1,
      red_penalty: hasDetails ? match.red.penalty : -1,
      blue_penalty: hasDetails ? match.blue.penalty : -1,
      red_auto_score: hasDetails ? match.red.auto : -1,
      blue_auto_score: hasDetails ? match.blue.auto : -1,
      red_tele_score: hasDetails ? match.red.teleop : -1,
      blue_tele_score: hasDetails ? match.blue.teleop : -1,
      red_end_score: hasDetails ? match.red.end : -1,
      blue_end_score: hasDetails ? match.blue.end : -1
    };

    if (isQual) {
      const { red, blue } = match.matchBrief;
      participants.push(buildParticipant(red.team1, 'R1', matchKey, red.isTeam1Surrogate));
      participants.push(buildParticipant(red.team2, 'R2', matchKey, red.isTeam2Surrogate));
      participants.push(buildParticipant(blue.team1, 'B1', matchKey, blue.isTeam1Surrogate));
      participants.push(buildParticipant(blue.team2, 'B2', matchKey, blue.isTeam2Surrogate));
    } else if (alliances && alliances.length > 0) {
      const red = alliances.filter((a) => a.seed === match.matchBrief.red.seed)[0];
      const blue = alliances.filter((a) => a.seed === match.matchBrief.blue.seed)[0];

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

    if (participants.length !== 4 && participants.length !== 6) return;

    log('Uploading match data for ' + matchKey, matchJSON);
    toaApi.put(`/event/${eventKey}/matches/${matchKey}`, [matchJSON]);
    toaApi.put(`/event/${eventKey}/matches/${matchKey}/participants`, participants);
    if (hasDetails) uploadMatchDetails(match, matchKey, eventKey);
  }

  async function retrieveTeams(teams) {
    const result = teams.map((team) => ({
      event_key: eventKey,
      event_participant_key: `${eventKey}-T${team.number}`,
      team_key: team.number.toString(),
      is_active: true,
      card_status: null
    }));
    return toaApi.delete(`/event/${eventKey}/teams`).finally(() => toaApi.post(`/event/${eventKey}/teams`, result));
  }

  async function retrieveRankings(rankingList) {
    if (rankingList.filter((r) => r.ranking > 0)) {
      const result = rankingList.map((rank) => ({
        rank_key: `${eventKey}-R${rank.team}`,
        event_key: eventKey,
        team_key: rank.team.toString(),
        rank: rank.ranking > 0 ? rank.ranking : 0,
        rank_change: 0,
        wins: rank.wins,
        losses: rank.losses,
        ties: rank.ties,
        highest_qual_score: rank.highestScore,
        ranking_points: rank.rankingPoints && rank.rankingPoints !== '--' ? parseFloat(rank.rankingPoints) || 0 : 0,
        qualifying_points: 0,
        tie_breaker_points:
          rank.tieBreakerPoints && rank.tieBreakerPoints !== '--' ? parseFloat(rank.tieBreakerPoints) || 0 : 0,
        disqualified: 0,
        played: rank.matchesPlayed
      }));
      log('Uploading rankings...', result);
      return toaApi
        .delete(`/event/${eventKey}/rankings`)
        .finally(() => toaApi.post(`/event/${eventKey}/rankings`, result));
    } else {
      log('No ranking to upload.', rankingList);
    }
  }
};

function generateMatchName(tournLevel, matchNumber, elimNumber) {
  if (tournLevel === 30) {
    return `Semis ${elimNumber} Match ${matchNumber}`;
  } else if (tournLevel === 4) {
    return `Finals ${matchNumber}`;
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
    station_status: isSurrogate ? 0 : noShow ? 2 : 1, // 0 = Surrogate, 1 = Normal, 2 = No Show/Sit Out
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
