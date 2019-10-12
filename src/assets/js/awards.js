const apis = require('../../apis');
const toaApi = apis.toa;
const scorekeeperApi = apis.scorekeeper;
const eventId = localStorage.getItem('SCOREKEEPER-EVENT');
const eventKey = localStorage.getItem('TOA-EVENT-KEY');

function uploadAwards() {
  const allAwards = [];
  scorekeeperApi.get(`/v2/events/${eventId}/awards/`).then((data) => {
    const awards = data.data.awards;
    for (const award of awards) {
      const awardId = getAwardIDFromName(award.awardName);
      if (!awardId) {
        continue;
      }

      for (const winner of award.winners) {
        const awardKey = `${eventKey}-${awardId}${winner.series || 0}`; //1819-ISR-CMP0-WIN1

        let teamKey = winner.team && winner.team > 0 ? winner.team + '' : null;
        let receiverName = null;
        if (awardId === 'DNSF' || awardId === 'DNSSF') {
          receiverName = `${winner.firstName} ${winner.lastName}`;
        }

        if (teamKey || receiverName) {
          allAwards.push({
            award_key: awardId + (winner.series || 0),
            award_name: getAwardNameFromID(awardId),
            awards_key: awardKey,
            event_key: eventKey,
            receiver_name: receiverName,
            team_key: teamKey
          });
        }
      }
    }
    console.log(allAwards);
    return toaApi.get('/event/' + eventKey + '/awards');
  }).then((oldAwards) =>{
    oldAwards = oldAwards.data.map((award) => award.awards_key);
    const toUpload = allAwards.filter((award) => !oldAwards.includes(award.awards_key));
    console.log('Uploading awards...', toUpload);
    return toaApi.post('/event/' + eventKey + '/awards', JSON.stringify(toUpload));
  }).catch((error) => {
    ui.showSnackbar('There was an error in uploading the awards');
  });
}

// Convert an award name to an award key
function getAwardIDFromName(name) {
  switch (name) {
    case 'Winning Alliance Award':
    case 'Winning Alliance':
      return 'WIN';
    case 'Finalist Alliance Award':
    case 'Finalist Alliance':
      return 'FIN';
    case 'Inspire Award':
      return 'INS';
    case 'Think Award':
      return 'THK';
    case 'Connect Award':
      return 'CNT';
    case 'Rockwell Collins Innovate Award':
    case 'Collins Aerospace Innovate Award':
      return 'INV';
    case 'Design Award':
      return 'DSN';
    case 'Motivate Award':
      return 'MOT';
    case 'Control Award':
      return 'CTL';
    case 'Promote Award':
      return 'PRM';
    case 'Compass Award':
      return 'CMP';
    case 'Judges\u0027 Award':
    case 'Judge\u0027s Award':
      return 'JUD';
    case 'Dean\u0027s List Semi-Finalists':
      return 'DNSSF';
    case 'Dean\u0027s List Winner':
      return 'DNSF';
    default:
      return null;
  }
}

// Convert an award key to an award name
function getAwardNameFromID(key) {
  if (key.startsWith('INS')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Inspire Award Winner';
    } else {
      return 'Inspire Award Finalist';
    }
  } else if (key.startsWith('THK')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Think Award Winner';
    } else {
      return 'Think Award Finalist';
    }
  } else if (key.startsWith('CNT')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Connect Award Winner';
    } else {
      return 'Connect Award Finalist';
    }
  } else if (key.startsWith('INV')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Rockwell Collins Innovate Award Winner';
    } else {
      return 'Rockwell Collins Innovate Award Finalist';
    }
  } else if (key.startsWith('DSN')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Design Award Winner';
    } else {
      return 'Design Award Finalist';
    }
  } else if (key.startsWith('MOT')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Motivate Award Winner';
    }else {
      return 'Motivate Award Finalist';
    }
  } else if (key.startsWith('CTL')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Control Award Winner';
    } else {
      return 'Control Award Finalist';
    }
  } else if (key.startsWith('PRM')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Promote Award Winner';
    } else {
      return 'Promote Award Finalist';
    }
  } else if (key.startsWith('CMP')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Compass Award Winner';
    } else {
      return 'Compass Award Finalist';
    }
  } else if (key.startsWith('JUD')) {
    if (key.substring(key.length - 1) === '1') {
      return 'Judges Award Winner';
    } else {
      return 'Judges Award Finalist';
    }
  } else if (key.startsWith('WIN')) {
    return 'Winning Alliance Award Winners';
  } else if (key.startsWith('FIN')) {
    return 'Finalist Alliance Award Winners';
  } else if (key.startsWith('DNSSF')) {
    return 'Dean\'s List Finalist Award';
  } else if (key.startsWith('DNSF')) {
    return 'Dean\'s List Winner Award';
  }
  return null;
}

module.exports = uploadAwards;
