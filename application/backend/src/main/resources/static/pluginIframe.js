(function (global) {

  function IframePlugin(options) {
    this.options = Object.assign({
      //local config
      pathIcon: './lib/plugin',   // icon default path 
      urlIframe: 'http://localhost:4200'
      //cluster config
      //pathIcon: 'http://aimusei.local/aimusei/api',   // icon default path 
      //urlIframe: 'http://aimusei.local/aimusei/'
    }, options || {});

    this.iconBtnMap = new Map();
    this._ICONBTN_ = 'iconBtn_';
	
    this.createShadowHost(); 
    this.injectCss();
    this.createIframe();
    this.addObserverContentBody();
    this.listenMessages();
    this.addCloseModalClick();
  }
  // Listener globale su evento chiusura modale
	 IframePlugin.prototype.addCloseModalClick = function () {
	   document.addEventListener("DOMContentLoaded", () => {
 
		if (this.closeBtn) {
		    this.closeBtn.addEventListener("click", () => {
			console.log("Modal chiusa via bottone close");
			this.closeModal();
		  });
		}
	  });
  }
  // Gestione messaggi da iframe
  IframePlugin.prototype.listenMessages = function () {
    window.addEventListener('message', event => {
      if (event.origin !== this.options.urlIframe || event.data.type !== 'saved') return;

      console.log("Iframe response:", event.data.body);
 
      let elem = this.closeBtn;
      const parsedBody = JSON.parse(JSON.stringify(event.data.body));
      const iconBtn = this.iconBtnMap.get(parsedBody.tag);

      if (elem && iconBtn) {
        elem.click();
        iconBtn.src = this.options.pathIcon + "/icona_" + (
          event.data?.status === 'ok' ? 'verde' :
          event.data?.status === 'ai' ? 'blu' : 'rossa'
        ) + ".png";
        iconBtn.parentElement.title = (
          event.data?.status === 'ok' ? 'Revisionato' :
          event.data?.status === 'ai' ? 'Semplificato' : 'Incompleto'
        );
      } else {
        console.log("Errore rif. icona non trovato " + iconBtn);
      }
    });
  }

  // Invio messaggi all’iframe
  IframePlugin.prototype.messageToIframe = function (tag, description) {
    const iframe = this.iframe 
	//const iframe = document.getElementById('widget-iframe');
    const iconBtn = this.iconBtnMap.get(tag);

    if (iframe && iconBtn) {
      iframe.contentWindow.postMessage({
        type: 'init',
        payload: {
          text: description,
          funz: "CREATE",
          tag: tag,
          title: "Title"+tag,
          status: iconBtn.src.includes('rossa.png') ? 'new' :
                  (iconBtn.src.includes('blu.png') ? 'ai' : 'verified'),
        }
      }, this.options.urlIframe);
    } else {
      console.log("Errore icona non trovata " + iconBtn);
    }
  }

  // Creazione iframe dinamico
  IframePlugin.prototype.createIframe = function () {
    if (!this.options.pathIcon)
      alert("ATTENTION: pathIcon is undefined");

    if (document.getElementById('widget-iframe'))
      return;

    const container = document.body;
 
   const divContainer = document.createElement("div");
    
    divContainer.innerHTML = 
      `<div class="modal fade" id="exampleModalAngular" style="opacity: 1;" tabindex="-1"
        aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div class="modal-dialog" style="display:content; max-width: 80vw; min-width: 80vw; max-height: 80vh; min-height: 80vh; width: 80vw; height: 80vh;">
          <div class="modal-content" style="height: 100%; width: 100%;">
            <div class="modal-header">
              <h1 class="modal-title" id="exampleModalLabel">Accessibilità</h1>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" id="closeAngularModal">
			    <svg xmlns="http://www.w3.org/2000/svg" 
				   viewBox="0 0 16 16" 
				   width="20" height="20" 
				   fill="currentColor">
				<path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 
						 8l2.647 2.646a.5.5 0 0 1-.708.708L8 
						 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 
						 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
			  </svg>
			  </button>
            </div>
            <div class="modal-body">
              <iframe src="${this.options.urlIframe}/backoffice-iframe" id="widget-iframe"
              style="width: 100%; height: 100%;"></iframe>
            </div>
          </div>
        </div>
      </div>`;
 
    this.shadowRoot.appendChild(divContainer);
    this.modal = this.shadowRoot.getElementById("exampleModalAngular");
	this.iframe = this.shadowRoot.getElementById('widget-iframe');
    this.closeBtn = this.shadowRoot.getElementById("closeAngularModal");
	    console.log('Iframe modal widget inserted', this.modal);

	//setTimeout(() => {
    //this.executeIframePluginTextArea();
    //this.executeIframePluginDivInput();
    
     //}, 500);
  };

 
  IframePlugin.prototype.executeIframePluginTextArea = function () {
    console.log("Observer DOM scanning for textarea...");

   // const textareas = document.querySelectorAll('textarea[editor="iframe"]');
    const textareas = document.querySelectorAll('textarea[editor="iframe"]');
    textareas.forEach((textarea, index) => {
      const tag = textarea.getAttribute('tag') || `AUTO_TAG_${index}`;
      const status = textarea.getAttribute('text-status') || 'NEW';
      const id = textarea.id || `textarea-${index}`;

      if (textarea.getAttribute('textarea-link') != null) return;

      console.log(`Searching in DOM new textarea founded with id=[${id}] applyied pluginIframe`);

      const wrapper = document.createElement('div');
      wrapper.className = 'd-flex align-items-center gap-3 mb-1';

      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'btn btn-link';
      button.title = 'Apri modale IFrame';
      //button.setAttribute('data-bs-toggle', 'modal');
      button.setAttribute('data-bs-target', '#exampleModalAngular');
      button.style.padding = '0';
      button.style.borderRadius = '20%';
      textarea.setAttribute('textarea-link', 'exampleModalAngular');

      const icon = document.createElement('img');
      icon.id = this._ICONBTN_ + tag;
      icon.alt = 'Icona stato';
      icon.style.width = '2rem';
      icon.style.height = '2rem';
      icon.setAttribute('textarea-tag', this._ICONBTN_ + tag);

      const statusToColor = {
        'NEW': 'rossa',
        'GENERATED-AI': 'blu',
        'REVISIONED': 'verde',
        'NEW_VERSION': 'rossa'
      };
      const iconColor = statusToColor[status] || 'rossa';
      icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

      this.iconBtnMap.set(tag, icon);

      // click con arrow function → mantiene il contesto this
      button.onclick = () => {
        window.currentTextArea = textarea;
		this.openModal();
        this.messageToIframe(tag, textarea.value);
      };

      button.appendChild(icon);
      wrapper.appendChild(button);
      textarea.parentNode.insertBefore(wrapper, textarea);
      console.log('added Iframe link by TAG:' + tag);
    });
  }

 IframePlugin.prototype.executeIframePluginDivInput = function () {
  console.log("Observer DOM scanning for tag div[editor] e input[editor]...");

  const editors = document.querySelectorAll('div[editor="iframe"], input[editor="iframe"]');

  editors.forEach((editor, index) => {
    const tag = editor.getAttribute('tag') || `AUTO_TAG_${index}`;
    const status = editor.getAttribute('text-status') || 'NEW';

    // skip duplicates 
    if (editor.getAttribute('textarea-link') != null) return;

    console.log(`Found editor [${tag}], attaching pluginIframe button`);

    // wrapper bottone
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'btn btn-link';
    button.title = 'Apri modale IFrame';
    button.style.padding = '0';
    button.style.borderRadius = '20%';
    editor.setAttribute('textarea-link', 'exampleModalAngular');

    const icon = document.createElement('img');
    icon.id = this._ICONBTN_ + tag;
    icon.alt = 'Icona stato';
    icon.style.width = '2rem';
    icon.style.height = '2rem';
    icon.setAttribute('textarea-tag', this._ICONBTN_ + tag);

    const statusToColor = {
      'NEW': 'rossa',
      'GENERATED-AI': 'blu',
      'REVISIONED': 'verde',
      'NEW_VERSION': 'rossa'
    };
    const iconColor = statusToColor[status] || 'rossa';
    icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

    this.iconBtnMap.set(tag, icon);
 
    button.onclick = () => {
      let extractedText = "";
      let paragraphs = null;
      window.currentEditor = editor;
      if (editor.tagName.toLowerCase() === "input") {
        extractedText = editor.value;
      } else {
        paragraphs = editor.querySelectorAll('p');
        extractedText = Array.from(paragraphs)
          .map(p => p.innerText.trim())
          .filter(t => t.length > 0)
          .join("\n\n");
      }
      console.log("Extracted text for iframe:", extractedText);

      this.openModal();
      this.messageToIframe(tag, extractedText);
    };

    button.appendChild(icon);
	let toolbar = null;
	 if (editor.tagName.toLowerCase() === "input") { 
       //find input wrapper item closest
       toolbar = editor.closest('.v-input__control')?.querySelector('.v-input__slot');
	   console.log(`Matched wrapper [${tag}] for Input`, toolbar);

	 } else {
       // find toolbar closest 
       toolbar = editor.closest('.quillWrapper')?.querySelector('.ql-toolbar.ql-snow');
	   	   console.log(`Matched wrapper [${tag}] for Toolbar`, toolbar);
	 }
    if (toolbar) {
      // avoid duplicates 
      if (toolbar.querySelector(`#${this._ICONBTN_ + tag}`)) {
        console.log(`Bottone already exist for TAG: ${tag}`);
        return;
      }
 
      const span = document.createElement('span');
      span.className = 'ql-formats';
      span.appendChild(button);

      toolbar.appendChild(span);
      console.log(`Added button inside Quill toolbar for TAG: ${tag}`);
    } else {
      console.warn(`Nessuna toolbar Quill trovata per TAG: ${tag}`);
    }

    console.log(`added Iframe link for tag html: ${editor.tagName.toLowerCase()} by IFRAME-TAG: ${tag}`);
  });
};
 
 ////

  IframePlugin.prototype.addObserverContentBody = function () {
    const observer = new MutationObserver(() => {
      this.executeIframePluginTextArea();
	  
      this.executeIframePluginDivInput();
      this.cleanupRemovedEditors();
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
	  attributes: true,
	  attributeFilter: ['style']   
});
  }
 IframePlugin.prototype.cleanupRemovedEditors = function () {
  console.log("Cleaning orphaned iframe buttons...");
 
  const existingTags = Array.from(
    document.querySelectorAll('div[editor="iframe"], input[editor="iframe"], textarea[editor="iframe"]')
  )
    .map(el => el.getAttribute('tag'))
    .filter(Boolean);

  // Scorre la mappa degli iconButton registrati
  this.iconBtnMap.forEach((iconElement, tag) => {
    if (!existingTags.includes(tag)) {
      console.warn(`🗑️ Removing orphaned button for tag: ${tag}`);

      // Cerca l'immagine nel DOM tramite id esatto
      const iconId = `${this._ICONBTN_}${tag}`;
      const imgElement = document.getElementById(iconId);

      if (imgElement) {
        // trova il container "reale" del bottone
        const buttonContainer =
          imgElement.closest('[data-btn-iframe]') || // caso v-tabs
          imgElement.closest('button')?.parentElement || // caso dentro un <span>
          imgElement.parentElement; // fallback finale

        if (buttonContainer && buttonContainer.parentElement) {
          buttonContainer.remove();
          console.log(`Removed DOM container for ${iconId}`);
        } else {
          console.warn(`No container found for ${iconId}`);
          imgElement.remove(); // fallback: elimina solo l’immagine
        }
      } else {
        console.warn(`⚠️ Image not found for tag: ${tag}`);
      }

      // Rimuove il riferimento dalla mappa
      this.iconBtnMap.delete(tag);
    }
  });

  console.log(`Cleanup completed. Active buttons: ${this.iconBtnMap.size}`);
};



  IframePlugin.prototype.openModal = function () {
	  //const modal = document.getElementById("exampleModalAngular");
	    const modal = this.modal;
	    if (modal) modal.classList.add('show');
		else alert ("Modal not found");
	 // const iframe = document.getElementById("widget-iframe"); 
 
	}
 
  IframePlugin.prototype.closeModal = function ()  {
		//const modal = document.getElementById("exampleModalAngular");
		const modal = this.modal;
	    if (modal) modal.classList.remove('show');
		else alert ("Modal not found");
  
	  // reset src dopo l’animazione (evita che continui a girare in background)
	  //setTimeout(() => {
		//iframe.src = "";
	  //}, 1500);
	}
 
  IframePlugin.prototype.addNewTextAreaSection = function () {
    const container = document.querySelector('#dynamic-sections') || document.body;

    const section = document.createElement('div');
    section.className = 'col-6 mt-4';

    section.innerHTML = `
      <div class="d-flex align-items-center gap-3">
        <label for="m-descrizione-sez2" class="font-label-semibold-sm">Descrizione Sezione 2 (Italiano)</label>
      </div>
      <textarea rows="5" 
                class="form-control" 
                id="m-descrizione-sez2" 
                tag="MUS1-SEZ2-ITA" 
                editor="iframe"
                disabled>
        Questa è una nuova sezione aggiunta dinamicamente per testare il plugin IFrame.
      </textarea>
    `;

    container.appendChild(section);
  }

  //create host in body for use Shadow DOM
  IframePlugin.prototype.createShadowHost = function () {
    if (document.getElementById("iframe-plugin-host")) {
      this.shadowHost = document.getElementById("iframe-plugin-host");
      this.shadowRoot = this.shadowHost.shadowRoot;
      return;
    }
    this.shadowHost = document.createElement("div");
    this.shadowHost.id = "iframe-plugin-host";
    document.body.appendChild(this.shadowHost);
    this.shadowRoot = this.shadowHost.attachShadow({ mode: "open" });
  };
/**
 * CSS Modal Style
 */
 
  
  IframePlugin.prototype.injectCss = function () {
    if (this.shadowRoot.querySelector("#iframe-plugin-styles")) return;

    const style = document.createElement("style");
    style.id = "iframe-plugin-styles";
    style.textContent = `
      @import url("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css");

      .modal {
        position: fixed;
        top: 0; left: 0;
        width: 100vw; height: 100vh;
        background-color: rgba(0, 0, 0, 0.6);
        opacity: 0;
        visibility: hidden;
        transition: opacity 0.4s ease, visibility 0.4s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1050;
      }

      .modal.show { opacity: 1; visibility: visible; }

      .modal-dialog {
        max-width: 80vw; min-width: 80vw;
        max-height: 80vh; min-height: 80vh;
        width: 80vw; height: 80vh;
        display: flex;
      }

      .modal-content {
        width: 100%; height: 100%;
        border-radius: 10px;
        overflow: hidden;
        background: #fff;
        box-shadow: 0px 4px 20px rgba(0,0,0,0.3);
        display: flex;
        flex-direction: column;
        animation: fadeInScale 0.3s ease;
      }

      @keyframes fadeInScale {
        from { transform: scale(0.9); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
      }

      .modal-header {
        background: #004080;
        color: #fff;
        padding: 14px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-family: Titillium Web, Helvetica, Arial, sans-serif;
      }

      .modal-title {
        font-size: 1.85rem;
        font-weight: 600;
        margin: 0;
      }

      .btn-close {
        cursor: pointer;
        background: transparent;
        border: none;
        font-size: 2rem;
        color: #fff;
      }

      .modal-body { flex: 1; padding: 0; }
      .modal-body iframe { width: 100%; height: 100%; border: none; }
    `;
    this.shadowRoot.appendChild(style);
  }; 
  
  global.IframePlugin = IframePlugin;

})(window);
