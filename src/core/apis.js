const axios = require('axios');
const axiosRetry = require('axios-retry');

const minScorekeeperVersion = '3.1.1';
const recommendScorekeeperVersion = '3.1.1';
const scorekeeperReleaseTag = 'v3.1.1';

const config = {
  scorekeeperSeason: '2022',
  toaSeason: '2122'
};

const scorekeeperFromIp = (ip) =>
  axios.create({
    baseURL: 'http://' + (ip || localStorage.getItem('SCOREKEEPER-IP')) + '/api',
    method: 'GET',
    timeout: 5000
  });

const toa = (apiKey) => {
  return axios.create({
    baseURL: 'https://theorangealliance.org/api',
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json',
      'X-TOA-Key': apiKey,
      'X-Application-Origin': 'TOA DataSync ' + (dataSyncVersion || '0.0.0')
    },
    data: null
  });
};

const cloud = (token) =>
  axios.create({
    baseURL: 'https://functions.theorangealliance.org',
    timeout: 10000,
    headers: {
      authorization: 'Bearer ' + token
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

module.exports = {
  scorekeeper,
  toa,
  scorekeeperFromIp,
  cloud,
  minScorekeeperVersion,
  recommendScorekeeperVersion,
  scorekeeperReleaseTag,
  config
};
