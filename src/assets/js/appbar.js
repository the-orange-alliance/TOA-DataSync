const { platform } = require('os');

function init() {
  if (platform() === 'darwin') {
    return;
  }

  const header = document.querySelector('.toa-toolbar') || document.querySelector('.drag-toolbar');
  const window = require('electron').remote.getCurrentWindow();


  // ${(location.href.indexOf("step1.html") > -1) ? '' : `<div class="button mdi mdi-logout" id="logout-button"></div>`}
  header.innerHTML = `<div id="window-controls">
        <div class="button" id="min-button" aria-label="minimize" title="Minimize" tabindex="-1">
          <svg aria-hidden="true" width="10" height="10">
            <path d="M 0,5 10,5 10,6 0,6 Z"></path>
          </svg>
        </div>
        <div class="button" id="close-button" aria-label="close" title="Close" tabindex="-1">
          <svg aria-hidden="true" width="10" height="10">
            <path d="M 0,0 0,0.7 4.3,5 0,9.3 0,10 0.7,10 5,5.7 9.3,10 10,10 10,9.3 5.7,5 10,0.7 10,0 9.3,0 5,4.3 0.7,0 Z"></path>
          </svg>
        </div>
    </div>` + header.innerHTML;

  header.style.paddingLeft = '0px';

  const closeButton = document.getElementById('close-button');
  const minButton = document.getElementById('min-button');
  const logoutButton = document.getElementById('logout-button');

  closeButton.addEventListener("click", event => {
    window.close();
  });

  minButton.addEventListener("click", event => {
    window.minimize();
  });
}

module.exports = { init };
