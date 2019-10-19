const { platform } = require('os');

function init() {
  if (platform() === 'darwin') {
    return;
  }

  const header = document.querySelector('.toa-toolbar') || document.querySelector('.drag-toolbar');
  const window = require('electron').remote.getCurrentWindow();


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

  document.body.innerHTML += `<div class="mdc-dialog" id="close-dialog" data-mdc-auto-init="MDCDialog">
      <div class="mdc-dialog__container">
          <div class="mdc-dialog__surface">
              <h2 class="mdc-dialog__title">Warning!</h2>
              <div class="mdc-dialog__content">
                Scores will stop uploading until you open the app again.
                Are you sure you want to close?
              </div>
              <footer class="mdc-dialog__actions">
                  <button type="button" class="mdc-button mdc-dialog__button" data-mdc-dialog-action="close-app">
                      <span class="mdc-button__label">Stop uploading</span>
                  </button>
                  <button type="button" class="mdc-button mdc-dialog__button" data-mdc-dialog-action="cancel">
                      <span class="mdc-button__label">Keep it open</span>
                  </button>
              </footer>
          </div>
      </div>
      <div class="mdc-dialog__scrim"></div>
  </div>`;
  mdc.autoInit();

  header.style.paddingLeft = '0px';

  const closeButton = document.getElementById('close-button');
  const minButton = document.getElementById('min-button');

  closeButton.addEventListener("click", event => {
    if (document.querySelector('.drag-toolbar')) {
      const dialog = document.querySelector('#close-dialog').MDCDialog;
      dialog.listen('MDCDialog:closed', async (data) => {
        if (data.detail.action === 'close-app') {
          window.close();
        }
      });
      dialog.open();
    } else {
      window.close();
    }
  });

  minButton.addEventListener("click", event => {
    window.minimize();
  });
}

module.exports = { init };
