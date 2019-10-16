const axios = require('axios');
const axiosRetry = require('axios-retry');

const minScorekeeperVersion = '1.1.1';

const scorekeeperFromIp = (ip) => axios.create({
  baseURL: 'http://' + (ip || localStorage.getItem('SCOREKEEPER-IP')) + '/api',
  timeout: 5000,
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
axiosRetry(scorekeeper, {
  retries: 1,
  retryCondition: (error) => {
    const notRetryable = [503, 404];
    return !error.response || !error.response.status || !notRetryable.includes(error.response.status);
  }
});

const toa = toaFromApiKey();

module.exports = { scorekeeper, toa, scorekeeperFromIp, toaFromApiKey, cloud, minScorekeeperVersion };
