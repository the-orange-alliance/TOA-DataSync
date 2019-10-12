const axios = require('axios');

const minScorekeeperVersion = '1.0.0';

const scorekeeperFromIp = (ip) => axios.create({
  baseURL: 'http://' + (ip || localStorage.getItem('SCOREKEEPER-IP')) + '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  data: {}
});

const toaFromApiKey = (apiKey) => {
  let key = apiKey;
  if (!key) {
    try {
      const index = parseInt(new URLSearchParams(window.location.search).get('i'), 10);
      const configEvent = JSON.parse(localStorage.getItem('CONFIG-EVENTS'))[index];
      key = configEvent.toa_api_key;
    } catch (e) {}
  }

  return axios.create({
    baseURL: 'https://theorangealliance.org/api',
    //baseURL: 'http://localhost:8008/api',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
      'X-TOA-Key': key,
      'X-Application-Origin': 'TOA DataSync 4.0',
    },
    data: {}
  });
};

const cloud = (token) => axios.create({
  baseURL: 'https://functions.theorangealliance.org',
  timeout: 10000,
  headers: {
    'authorization': 'Bearer ' + token,
  },
  data: {}
});

const scorekeeper = scorekeeperFromIp();
const toa = toaFromApiKey();

module.exports = { scorekeeper, toa, scorekeeperFromIp, toaFromApiKey, cloud, minScorekeeperVersion };
