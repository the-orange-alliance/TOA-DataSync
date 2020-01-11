const axios = require('axios');
const axiosRetry = require('axios-retry');

const minScorekeeperVersion = '1.2.1';
const recommendScorekeeperVersion = '1.3.0';
const scorekeeperReleaseTag = 'v1.3.0';

const getSKBaseUrl = (ip) => {
  const host = ip || localStorage.getItem('SCOREKEEPER-IP');
  try {
    const url = new URL(host);;
    return url.protocol + '//' + url.hostname;
  } catch (e) {
    return 'http://' + host;
  }
};

const scorekeeperFromIp = (ip) => axios.create({
  baseURL: getSKBaseUrl(ip) + '/api',
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
      'X-Application-Origin': 'TOA DataSync ' + (dataSyncVersion || '0.0.0'),
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

module.exports = {
  scorekeeper, toa, scorekeeperFromIp, cloud, minScorekeeperVersion, recommendScorekeeperVersion, scorekeeperReleaseTag, getSKBaseUrl
};
