let log = '';

exports.write = (...args) => {
  const date = `[${new Date().toISOString()}] `;
  const msg = args.map(msg => typeof msg === "object" ? JSON.stringify(msg) : msg).join(', ');
  log += date + msg + '\n';
};

exports.getLog = () => log;
