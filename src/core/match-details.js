const apis = require('./apis');
const events = JSON.parse(localStorage.getItem('CONFIG-EVENTS'));
const toaApi = apis.toa;

module.exports = async function uploadMatchDetails(match, matchKey, eventKey) {
  let apiKeys = {};
  for (const event of events) {
    apiKeys[event.toa_event_key] = event.toa_api_key;
  }

  let data = {};
  if (matchKey.startsWith('1819')) {
    data = getMatchDetails1819(match, matchKey);
  } else if (matchKey.startsWith('1920')) {
    data = getMatchDetails1920(match, matchKey);
  } else if (matchKey.startsWith('2021')) {
    data = getMatchDetails2021(match, matchKey);
  }

  return toaApi(apiKeys[eventKey]).put(`/event/${eventKey}/matches/${matchKey}/details`, JSON.stringify([data]));
};

function getMatchDetails2021(details, matchKey) {
  const { red, blue } = details;
  const parseWobbleString = (key) => {
    if (key === 'NONE') return 0;
    if (key === 'START_LINE') return 1;
    if (key === 'DROP_ZONE') return 2;
  };

  return {
    match_key: matchKey,
    match_detail_key: matchKey + '-DTL',
    // randomization: details.randomization

    red_min_pen: details.blue.minorPenalties,
    red_maj_pen: details.blue.majorPenalties,
    blue_min_pen: details.red.minorPenalties,
    blue_maj_pen: details.red.majorPenalties,

    red: {
      auto_wobble_delivered_1: red.wobbleDelivered1,
      auto_wobble_delivered_2: red.wobbleDelivered2,
      auto_nav_pts: red.navigationPoints,
      auto_navigated_1: red.navigated1,
      auto_navigated_2: red.navigated2,
      auto_tower_points: red.autoTowerPoints,
      auto_tower_low: red.autoTowerLow,
      auto_tower_mid: red.autoTowerMid,
      auto_tower_high: red.autoTowerHigh,
      auto_power_shot_points: red.autoPowerShotPoints,
      auto_power_shot_left: red.autoPowerShotLeft,
      auto_power_shot_center: red.autoPowerShotCenter,
      auto_power_shot_right: red.autoPowerShotRight,
      auto_wobble_points: red.autoWobblePoints,

      // tele_tower_points: red.driverControlledTowerPoints,
      tele_tower_low: red.driverControlledTowerLow,
      tele_tower_mid: red.driverControlledTowerMid,
      tele_tower_high: red.driverControlledTowerHigh,

      end_wobble_points: red.endgameWobblePoints,
      end_wobble_1: parseWobbleString(red.wobbleEnd1),
      end_wobble_2: parseWobbleString(red.wobbleEnd2),
      end_wobble_ring_points: red.endgamePowerShotPoints,
      end_wobble_rings_1: red.wobbleRings1,
      end_wobble_rings_2: red.wobbleRings2,
      end_power_shot_points: red.endgamePowerShotPoints,
      end_power_shot_left: red.endgamePowerShotLeft,
      end_power_shot_center: red.endgamePowerShotCenter,
      end_power_shot_right: red.endgamePowerShotRight
    },

    blue: {
      auto_wobble_delivered_1: blue.wobbleDelivered1,
      auto_wobble_delivered_2: blue.wobbleDelivered2,
      auto_nav_pts: blue.navigationPoints,
      auto_navigated_1: blue.navigated1,
      auto_navigated_2: blue.navigated2,
      auto_tower_points: blue.autoTowerPoints,
      auto_tower_low: blue.autoTowerLow,
      auto_tower_mid: blue.autoTowerMid,
      auto_tower_high: blue.autoTowerHigh,
      auto_power_shot_points: blue.autoPowerShotPoints,
      auto_power_shot_left: blue.autoPowerShotLeft,
      auto_power_shot_center: blue.autoPowerShotCenter,
      auto_power_shot_right: blue.autoPowerShotRight,
      auto_wobble_points: blue.autoWobblePoints,

      // tele_tower_points: blue.driverControlledTowerPoints,
      tele_tower_low: blue.driverControlledTowerLow,
      tele_tower_mid: blue.driverControlledTowerMid,
      tele_tower_high: blue.driverControlledTowerHigh,

      end_wobble_points: blue.endgameWobblePoints,
      end_wobble_1: parseWobbleString(blue.wobbleEnd1),
      end_wobble_2: parseWobbleString(blue.wobbleEnd2),
      end_wobble_ring_points: blue.endgamePowerShotPoints,
      end_wobble_rings_1: blue.wobbleRings1,
      end_wobble_rings_2: blue.wobbleRings2,
      end_power_shot_points: blue.endgamePowerShotPoints,
      end_power_shot_left: blue.endgamePowerShotLeft,
      end_power_shot_center: blue.endgamePowerShotCenter,
      end_power_shot_right: blue.endgamePowerShotRight
    }
  };
}

function getMatchDetails1920(details, matchKey) {
  const getCapLevel = (alliance) => {
    const level1 = alliance.robot1.capstoneLevel === -1 ? 0 : alliance.robot1.capstoneLevel;
    const level2 = alliance.robot2.capstoneLevel === -1 ? 0 : alliance.robot2.capstoneLevel;
    return level1 + level2;
  };
  const getSkystones = (alliance) => {
    if (alliance.autoStones[0] === 'SKYSTONE' && alliance.autoStones[1] === 'SKYSTONE') {
      return 2;
    } else if (alliance.autoStones[0] === 'SKYSTONE' || alliance.autoStones[1] === 'SKYSTONE') {
      return 1;
    } else {
      return 0;
    }
  };
  const getStones = (alliance) => {
    const skystones = getSkystones(alliance);
    const stones = alliance.autoStones.filter((x) => x && x !== 'NONE').length - skystones;
    return Math.max(stones, 0); // Prevent negative numbers
  };

  return {
    match_key: matchKey,
    match_detail_key: matchKey + '-DTL',

    red_min_pen: details.blue.minorPenalties,
    red_maj_pen: details.blue.majorPenalties,
    blue_min_pen: details.red.minorPenalties,
    blue_maj_pen: details.red.majorPenalties,

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

function getMatchDetails1819(details, matchKey) {
  const calcCratePoints = (parking) => {
    let partCrater = 0;
    let fullCrater = 0;
    switch (parking) {
      case 15:
        partCrater = 1;
        break;
      case 25:
        fullCrater = 1;
        break;
      case 30:
        partCrater = 2;
        break;
      case 40:
        partCrater = 1;
        fullCrater = 1;
        break;
      case 50:
        fullCrater = 2;
        break;
      default:
        partCrater = parking;
        break; // We should never get here
    }
    return { partCrater, fullCrater };
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
    blue_end_comp: calcCratePoints(details.blue.endParking).fullCrater
  };
}
