const apis = require('../../apis');
const toaApi = apis.toa;

module.exports = async function uploadMatchDetails(details, matchKey, eventKey, isUpdate) {
  if (!details || details.resultPostedTime === -1) {
    return;
  }

  let data = {};
  if (matchKey.startsWith('1819')) {
    data = getMatchDetails1819(details, matchKey);
  } else if (matchKey.startsWith('1920')) {
    data = getMatchDetails1920(details, matchKey);
  }

  if (isUpdate) {
    return await toaApi.put(`/event/${eventKey}/matches/${matchKey}/details`, JSON.stringify([data]));
  } else {
    return await toaApi.post(`/event/${eventKey}/matches/details`, JSON.stringify([data]));
  }
};

function getMatchDetails1819(details, matchKey) {
  const calcCratePoints = (parking) => {
    let partCrater = 0;
    let fullCrater = 0;
    switch (parking) {
      case 15: partCrater = 1; break;
      case 25: fullCrater = 1; break;
      case 30: partCrater = 2; break;
      case 40: partCrater = 1; fullCrater = 1; break;
      case 50: fullCrater = 2; break;
      default: partCrater = parking; break; // We should never get here
    }
    return {partCrater, fullCrater};
  };

  return {
    match_key: matchKey,
    match_detail_key: matchKey + '-DTL',

    red_min_pen: details.red.minorPenalties,
    red_maj_pen: details.red.majorPenalties,
    red_auto_land: details.red.landed / 30,
    red_auto_samp: details.red.mineralSample / 25,
    red_auto_claim: details.red.claimedDepot / 15,
    red_auto_park: details.red.autoParking / 10,
    red_tele_gold: details.red.landerGold / 5,
    red_tele_silver: details.red.landerSilver / 5,
    red_tele_depot: details.red.depotMinerals / 2,
    red_end_latch: details.red.latchedLander / 50,
    red_end_in: calcCratePoints(details.red.endParking).partCrater,
    red_end_comp: calcCratePoints(details.red.endParking).fullCrater,

    blue_min_pen: details.blue.minorPenalties,
    blue_maj_pen: details.blue.majorPenalties,
    blue_auto_land: details.blue.landed / 30,
    blue_auto_samp: details.blue.mineralSample / 25,
    blue_auto_claim: details.blue.claimedDepot / 15,
    blue_auto_park: details.blue.autoParking / 10,
    blue_tele_gold: details.blue.landerGold / 5,
    blue_tele_silver: details.blue.landerSilver / 5,
    blue_tele_depot: details.blue.depotMinerals / 2,
    blue_end_latch: details.blue.latchedLander / 50,
    blue_end_in: calcCratePoints(details.blue.endParking).partCrater,
    blue_end_comp: calcCratePoints(details.blue.endParking).fullCrater,
  };
}

function getMatchDetails1920(details, matchKey) {
  const getCapLevel = (alliance) => {
    const level1 = alliance.robot1.capstoneLevel === -1 ? 0 : alliance.robot1.capstoneLevel;
    const level2 = alliance.robot2.capstoneLevel === -1 ? 0 : alliance.robot2.capstoneLevel;
    return level1 + level2;
  };
  const getSkystones = (alliance) => {
    if (alliance.autoStones[0] === 'SKYSTONE' && alliance.autoStones[0] === 'SKYSTONE') {
      return 2;
    } else if (alliance.autoStones[0] !== 'SKYSTONE' || alliance.autoStones[1] === 'SKYSTONE') {
      return 1;
    } else {
      return 0;
    }
  };
  const getStones = (alliance) => {
    const skystones = getSkystones(alliance);
    return alliance.autoStones.filter(x => x && x !== 'NONE').length - skystones;
  };

  return {
    match_key: matchKey,
    match_detail_key: matchKey + '-DTL',

    red_min_pen: details.red.minorPenalties,
    red_maj_pen: details.red.majorPenalties,
    blue_min_pen: details.blue.minorPenalties,
    blue_maj_pen: details.blue.majorPenalties,

    red: {
      auto_stone_1: details.red.autoStones[0],
      auto_stone_2: details.red.autoStones[1],
      auto_stone_3: details.red.autoStones[2],
      auto_stone_4: details.red.autoStones[3],
      auto_stone_5: details.red.autoStones[4],
      auto_stone_6: details.red.autoStones[5],
      auto_delivered_skystones: getSkystones(details.red),
      auto_delivered_stones: getStones(details.red),
      auto_returned: details.red.autoReturned,
      first_returned_is_skystone: details.red.firstReturnedIsSkystone,
      auto_placed: details.red.autoPlaced,
      foundation_repositioned: details.red.foundationRepositioned,
      tele_delivered: details.red.driverControlledDelivered,
      tele_returned: details.red.driverControlledReturned,
      tele_placed: details.red.driverControlledPlaced,
      robot_1: {
        nav: details.red.robot1.navigated,
        parked: details.red.robot1.parked,
        cap_level: details.red.robot1.capstoneLevel
      },
      robot_2: {
        nav: details.red.robot2.navigated,
        parked: details.red.robot2.parked,
        cap_level: details.red.robot2.capstoneLevel
      },
      foundation_moved: details.red.foundationMoved,
      auto_transport_points: details.red.autoTransportPoints,
      auto_placed_points: details.red.autoPlacedPoints,
      reposition_points: details.red.repositionPoints,
      nav_points: details.red.navigationPoints,
      tele_transport_points: details.red.driverControlledTransportPoints,
      tele_placed_points: details.red.driverControlledPlacedPoints,
      tower_bonus: details.red.towerBonusPoints / 2,
      tower_capping_bonus: (details.red.capstonePoints - getCapLevel(details.red)) / 5,
      tower_level_bonus: getCapLevel(details.red),
      end_robots_parked: details.red.parkingPoints / 5,
      auto_points: details.red.autonomousPoints,
      auto_total: details.red.auto,
      tele_total: details.red.tele,
      end_total: details.red.end,
      penalty_total: details.red.penalty
    },

    blue: {
      auto_stone_1: details.blue.autoStones[0],
      auto_stone_2: details.blue.autoStones[1],
      auto_stone_3: details.blue.autoStones[2],
      auto_stone_4: details.blue.autoStones[3],
      auto_stone_5: details.blue.autoStones[4],
      auto_stone_6: details.blue.autoStones[5],
      auto_delivered_skystones: getSkystones(details.blue),
      auto_delivered_stones: getStones(details.blue),
      auto_returned: details.blue.autoReturned,
      first_returned_is_skystone: details.blue.firstReturnedIsSkystone,
      auto_placed: details.blue.autoPlaced,
      foundation_repositioned: details.blue.foundationRepositioned,
      tele_delivered: details.blue.driverControlledDelivered,
      tele_returned: details.blue.driverControlledReturned,
      tele_placed: details.blue.driverControlledPlaced,
      robot_1: {
        nav: details.blue.robot1.navigated,
        parked: details.blue.robot1.parked,
        cap_level: details.blue.robot1.capstoneLevel
      },
      robot_2: {
        nav: details.blue.robot2.navigated,
        parked: details.blue.robot2.parked,
        cap_level: details.blue.robot2.capstoneLevel
      },
      foundation_moved: details.blue.foundationMoved,
      auto_transport_points: details.blue.autoTransportPoints,
      auto_placed_points: details.blue.autoPlacedPoints,
      reposition_points: details.blue.repositionPoints,
      nav_points: details.blue.navigationPoints,
      tele_transport_points: details.blue.driverControlledTransportPoints,
      tele_placed_points: details.blue.driverControlledPlacedPoints,
      tower_bonus: details.blue.towerBonusPoints / 2,
      tower_capping_bonus: (details.blue.capstonePoints - getCapLevel(details.blue)) / 5,
      tower_level_bonus: getCapLevel(details.blue),
      end_robots_parked: details.blue.parkingPoints / 5,
      auto_points: details.blue.autonomousPoints,
      auto_total: details.blue.auto,
      tele_total: details.blue.tele,
      end_total: details.blue.end,
      penalty_total: details.blue.penalty
    },
    randomization: details.randomization
  };
}
