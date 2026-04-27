(function (global) {
  function IframePlugin(options) {
    this.options = Object.assign(
      {
        pathIcon: "https://api-coll.museiitaliani.it/aimusei/api",
        urlIframe: "https://api-coll.museiitaliani.it/aimusei/",
        observerDom: false,
        autoCheckStatus: true,  // ← abilita/disabilita auto-refresh
        token: '',
        idMuseo: ''
      },
      options || {}
    );

    this.iconBtnMap = new Map();
    this._ICONBTN_ = "iconBtn_";
    this._autoTagSeq = 0;
    this.token = this.options.token;
    this.idMuseo = this.options.idMuseo;
    this._lastStatusText = new Map();

    this.createShadowHost();
    this.injectCss();
    setTimeout(() => {
      this.createIframe();
      this.initPostMessageChannel();
      this.addObserverContentBody();
      this.listenMessages();
    }, 3000);
  }

  // listener globale su evento chiusura modale
  IframePlugin.prototype.addCloseModalClick = function () {
    const bind = () => {
      if (!this.closeBtn) return;
      this.closeBtn.addEventListener("click", () => {
        console.log("Modal chiusa via bottone close");
        this.closeModal();
      });
    };

    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", bind, { once: true });
    } else {
      bind();
    }
  };

  // gestione messaggi da iframe di salvataggio o check status
  IframePlugin.prototype.listenMessages = function () {
    // evita doppio bind se chiamata più volte
    if (this._onMessageMain) return;
    if (this._destroyed) return;

    this._onMessageMain = (event) => {
      if (event.origin !== this._targetOrigin) return;
      if (!["saved", "check-status"].includes(event.data.type)) return;

      console.log("Iframe response:", event.data.body);

      const parsedBody = JSON.parse(JSON.stringify(event.data.body));
      const iconBtn = this.iconBtnMap.get(parsedBody.tag);

      if (!iconBtn) return;

      if (event.data.type === "saved" && this.closeBtn)
        this.closeBtn.click();
      iconBtn.src =
        this.options.pathIcon +
        "/icona_" +
        (event.data?.status === "ok"
          ? "verde"
          : event.data?.status === "ai"
          ? "blu"
          : "rossa") +
        ".png";
      let tooltip =
        event.data?.status === "ok"
          ? "Revisionato"
          : event.data?.status === "ai"
          ? "Semplificato"
          : "Incompleto";
      iconBtn.parentElement.title = tooltip;
    };

    window.addEventListener("message", this._onMessageMain);
  };

  // invio messaggi all’iframe
  IframePlugin.prototype.messageToIframe = function (tag, description, context, title) {
    const iconBtn = this.iconBtnMap.get(tag);
    if (iconBtn) {
      this.postToIframe({
        type: "init",
        payload: {
          text: description,
          funz: "CREATE",
          tag: tag,
          context: context ? context : 'ETR',
          canGeneratePdf: context === "INFO_MUSEO",
          token: this.token,
          idMuseo: this.idMuseo,
          title: title ? title : ("Title " + tag),
          status: iconBtn.src.includes("rossa.png") ? "new" : (iconBtn.src.includes("blu.png") ? "ai" : "verified"),
        }
      });
    } else {
      console.log("Errore icona non trovata " + iconBtn);
    }
  };

  // creazione iframe dinamico
  IframePlugin.prototype.createIframe = function () {
    if (!this.options.pathIcon) alert("ATTENTION: pathIcon is undefined");

    if (this.shadowRoot?.getElementById("widget-iframe")) return;

    const base = this.options.urlIframe.replace(/\/+$/, "");

    const divContainer = document.createElement("div");

    divContainer.innerHTML = `<div class="modal fade" id="exampleModalAngular" style="opacity: 1;" tabindex="-1"
        aria-labelledby="exampleModalLabel">
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
              <iframe src="${base}/backoffice-iframe" id="widget-iframe"
              style="width: 100%; height: 100%;"></iframe>
            </div>
          </div>
        </div>
      </div>`;

    this.shadowRoot.appendChild(divContainer);
    this.modal = this.shadowRoot.getElementById("exampleModalAngular");
    this.iframe = this.shadowRoot.getElementById("widget-iframe");
    this.closeBtn = this.shadowRoot.getElementById("closeAngularModal");
    console.log("Iframe modal widget inserted", this.modal);

    this.addCloseModalClick();

    setTimeout(() => {
      this.executeIframePluginTextArea();
      this.executeIframePluginDivInput();
    }, 500);
  };

  // invio alla coda se iframe non pronta, altrimenti invio diretto per il controllo dello stato
  IframePlugin.prototype.checkStatus = function (tag, description) {
    if (this._destroyed) return;
    if (!tag) return;
    const text = (description ?? "").toString();
    if (text.trim().length === 0) return;

    this.postToIframe({
      type: "status-button",
      payload: { text, tag, token: this.token, }
    });
  }

  // estrazione testo dal div content editable
  IframePlugin.prototype.extractTextAny = function (el) {
    if (!el) return "";

    const tag = (el.tagName || "").toLowerCase();

    // input/textarea/select
    if (tag === "textarea" || tag === "input" || tag === "select") {
      return (el.value ?? "").toString();
    }

    // Quill: prova istanza (spesso è appesa come __quill)
    const quill =
      el.__quill ||
      el.querySelector?.(".ql-editor")?.__quill ||
      el.querySelector?.(".ql-container")?.__quill;

    if (quill && typeof quill.getText === "function") {
      return (quill.getText() ?? "").toString().trimEnd();
    }

    // Quill: testo dentro .ql-editor
    const qlEditor = el.classList?.contains("ql-editor")
      ? el
      : el.querySelector?.(".ql-editor");

    if (qlEditor) {
      return (qlEditor.innerText ?? qlEditor.textContent ?? "").toString();
    }

    // contenteditable generico
    const ce = el.isContentEditable ? el : el.querySelector?.('[contenteditable="true"]');
    if (ce) {
      return (ce.innerText ?? ce.textContent ?? "").toString();
    }

    // fallback
    return (el.innerText ?? el.textContent ?? "").toString();
  };

  // scansione del DOM per textarea con attributo editor=iframe, estrazione testo e inserimento bottone per apertura modale, con gestione stato tramite icona colorata
  IframePlugin.prototype.executeIframePluginTextArea = function () {
    console.log("Observer DOM scanning for textarea...");

    const textareas = deepQueryAll(document, 'textarea[editor="iframe"]');

    textareas.forEach((textarea, index) => {
      const tag = this._ensureTag(textarea, "AUTO_TA");
      const status = textarea.getAttribute("text-status") || "NEW";
      const id = textarea.id || `textarea-${index}`;
      const title = textarea.getAttribute("data-title");
      const context = textarea.getAttribute("context");

      if (textarea.getAttribute("textarea-link") != null)
        return;

      console.log(`Searching in DOM new textarea founded with id=[${id}] applyied pluginIframe`);

      const wrapper = document.createElement("div");
      wrapper.className = "d-flex align-items-center gap-3 mb-1";
      wrapper.setAttribute("data-btn-iframe", "1");

      const button = document.createElement("button");
      button.type = "button";
      button.className = "btn btn-link";
      button.title = ["ETR"].includes(context) ? 'Easy to read non pronto' : "Apri modale IFrame";
      button.setAttribute("data-bs-target", "#exampleModalAngular");
      button.style.padding = "0";
      button.style.borderRadius = "20%";
      textarea.setAttribute("textarea-link", "exampleModalAngular");

      const icon = document.createElement("img");
      icon.id = this._ICONBTN_ + _safeId(tag);
      icon.alt = "Icona stato";
      icon.style.width = "2rem";
      icon.style.height = "2rem";
      icon.setAttribute("textarea-tag", this._ICONBTN_ + tag);

      const statusToColor = {
        NEW: "rossa",
        "GENERATED-AI": "blu",
        REVISIONED: "verde",
        NEW_VERSION: "rossa",
      };

      const iconColor = statusToColor[status] || "rossa";
      icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

      this.iconBtnMap.set(tag, icon);

      this._attachStatusWatch(textarea, tag, () => this.extractTextAny(textarea));

      // check status button
      if (this.options.autoCheckStatus) {
        this.checkStatus(tag, this.extractTextAny(textarea));
      }

      button.onclick = () => {
        window.currentTextArea = textarea;
        this.openModal();
        this.messageToIframe(tag, this.extractTextAny(textarea), context, title);
      };

      button.appendChild(icon);
      wrapper.appendChild(button);
      textarea.parentNode.insertBefore(wrapper, textarea);
      console.log("added Iframe link by TAG:" + tag);
    });
  };

  // scansione del DOM per div o input con attributo editor=iframe, estrazione testo e inserimento bottone per apertura modale, con gestione stato tramite icona colorata
  IframePlugin.prototype.executeIframePluginDivInput = function () {
    console.log("Observer DOM scanning for tag div[editor] e input[editor]...");

    const editors = deepQueryAll(document, 'div[editor="iframe"], input[editor="iframe"]');

    editors.forEach((editor, index) => {
      const tag = this._ensureTag(editor, "AUTO_ED");
      const status = editor.getAttribute("text-status") || "NEW";
      const title = editor.getAttribute("data-title");
      const context = editor.getAttribute("context");

      // skip duplicates
      if (editor.getAttribute("textarea-link") != null)
        return;

      console.log(`Found editor [${tag}], attaching pluginIframe button`);

      const button = document.createElement("button");
      button.type = "button";
      button.className = "btn btn-link";
      button.title = "Apri modale IFrame";
      button.style.padding = "0";
      button.style.borderRadius = "20%";
      editor.setAttribute("textarea-link", "exampleModalAngular");

      const icon = document.createElement("img");
      icon.id = this._ICONBTN_ + _safeId(tag);
      icon.alt = "Icona stato";
      icon.style.width = "2rem";
      icon.style.height = "2rem";
      icon.setAttribute("textarea-tag", this._ICONBTN_ + tag);

      const statusToColor = {
        NEW: "rossa",
        "GENERATED-AI": "blu",
        REVISIONED: "verde",
        NEW_VERSION: "rossa",
      };

      const iconColor = statusToColor[status] || "rossa";
      icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

      this.iconBtnMap.set(tag, icon);

      this._attachStatusWatch(editor, tag, () => this.extractTextAny(editor));

      // check status button
      let extractedText = this.extractTextAny(editor);
      if (this.options.autoCheckStatus) {
        this.checkStatus(tag, extractedText);
      }

      button.onclick = () => {
        window.currentEditor = editor;
        extractedText = this.extractTextAny(editor);
        console.log("Extracted text for iframe:", extractedText);
        this.openModal();
        this.messageToIframe(tag, extractedText, context, title);
      };

      button.appendChild(icon);
      let toolbar = null;
      if (editor.tagName.toLowerCase() === "input") {
        // find input wrapper item closest
        toolbar = editor.parentElement;
        console.log(`Matched wrapper [${tag}] for Input`, toolbar);
      } else {
        // find toolbar closest
        toolbar =
          editor.closest(".quillWrapper")?.querySelector(".ql-toolbar") ||
          editor.closest(".ql-container")?.parentElement?.querySelector(".ql-toolbar") ||
          editor.parentElement?.querySelector?.(".ql-toolbar");
        console.log(`Matched wrapper [${tag}] for Toolbar`, toolbar);
      }
      if (toolbar) {
        // avoid duplicates
        if (toolbar.querySelector(`#${this._ICONBTN_ + tag}`)) {
          console.log(`Bottone already exist for TAG: ${tag}`);
          return;
        }

        const span = document.createElement("span");
        span.className = "ql-formats";
        span.appendChild(button);
        span.setAttribute("data-btn-iframe", "1");

        toolbar.appendChild(span);
        console.log(`Added button inside Quill toolbar for TAG: ${tag}`);
      } else {
        console.warn(`Nessuna toolbar Quill trovata per TAG: ${tag}`);
      }

      console.log(`added Iframe link for tag html: ${editor.tagName.toLowerCase()} by IFRAME-TAG: ${tag}`);
    });
  };

  // aggiunta observer su body per rilevare dinamicamente nuovi editor aggiunti dopo il caricamento iniziale, con possibilità di attivazione/disattivazione tramite opzione
  IframePlugin.prototype.addObserverContentBody = function () {
    let scheduled = false;
    const run = () => {
      scheduled = false;
      this.executeIframePluginTextArea();
      this.executeIframePluginDivInput();
      this.cleanupRemovedEditors();
    };
    this.observer = new MutationObserver(() => {
      if (scheduled) return;
      scheduled = true;
      setTimeout(run, 100);
    });

    // option default
    if (this.options.observerDom) this.startObserver();
  };

  // ferma l'observer per evitare esecuzioni multiple durante operazioni di pulizia o modifiche dinamiche importanti, da riavviare manualmente dopo con startObserver
  IframePlugin.prototype.stopObserver = function () {
    if (this.observer) {
      this.observer.disconnect();
      console.log("IframePlugin: MutationObserver stopped successfully.");
    } else {
      console.warn("IframePlugin: Observer not available or already stopped.");
    }
  };

  // riavvia l'observer, utile dopo operazioni di pulizia o modifiche dinamiche importanti per assicurarsi che il plugin rilevi correttamente i nuovi editor o quelli rimossi
  IframePlugin.prototype.startObserver = function () {
    if (this.observer) {
      this.observer.observe(document.body, {
        childList: true,
        subtree: true,
        // attributes: true,
        // attributeFilter: ["style", "class", "disabled"]
      });
      console.log("IframePlugin: MutationObserver (re)started successfully.");
    } else {
      console.error(
        "IframePlugin: Cannot start, observer instance not found. Was the plugin initialized correctly?"
      );
    }
  };

  // rimuove i bottoni associati a editor che non esistono più nel DOM, per evitare accumulo di bottoni orfani dopo modifiche dinamiche
  IframePlugin.prototype.cleanupRemovedEditors = function () {
    console.log("Cleaning orphaned iframe buttons...");

    const existingTags = Array.from(
      deepQueryAll(document, 'div[editor="iframe"], input[editor="iframe"], textarea[editor="iframe"]')
    )
      .map((el) => el.getAttribute("tag"))
      .filter(Boolean);

    this.iconBtnMap.forEach((iconElement, tag) => {
      if (existingTags.includes(tag)) return;

      console.warn(`🗑️ Removing orphaned button for tag: ${tag}`);

      const imgElement = iconElement;
      if (!imgElement || !imgElement.isConnected) {
        this.iconBtnMap.delete(tag);
        return;
      }

      const buttonContainer =
        imgElement.closest("[data-btn-iframe]") ||
        imgElement.closest("button")?.parentElement ||
        imgElement.parentElement;

      if (buttonContainer?.parentElement) {
        buttonContainer.remove();
      } else {
        imgElement.remove();
      }

      this.iconBtnMap.delete(tag);
    });

    console.log(`Cleanup completed. Active buttons: ${this.iconBtnMap.size}`);
  };

  // chiusura modale iframe
  IframePlugin.prototype.openModal = function () {
    if (!this.modal) return;
    const modal = this.modal;
    modal.classList.add("show");
    modal.style.display = "flex";
    modal.animate([{ opacity: 0 }, { opacity: 1 }], {
      duration: 300,
      fill: "forwards",
    });
  };

  // apertura modale iframe
  IframePlugin.prototype.closeModal = function () {
    if (!this.modal) return;
    const modal = this.modal;
    const fadeOut = modal.animate([{ opacity: 1 }, { opacity: 0 }], {
      duration: 300,
      fill: "forwards",
    });
    fadeOut.onfinish = () => {
      modal.classList.remove("show");
      modal.style.display = "none";
    };
  };

  // creazione host nel body per uso Shadow DOM
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

  // css Modal Style
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

  // inizializzazione canale di comunicazione postMessage con l'iframe, gestione stato ready e coda messaggi
  IframePlugin.prototype.initPostMessageChannel = function () {
    this._targetOrigin = new URL(this.options.urlIframe).origin;
    this._pmReady = false;
    this._pmQueue = new Map();
    this._destroyed = false;
    this._pingTimer = null;

    // quando l'iframe ricarica, torna non-ready
    this._onIframeLoad = () => {
      this._pmReady = false;
      // ping
      this._pingIframeReady();
    };
    this.iframe.addEventListener("load", this._onIframeLoad);

    // ricezione messaggi dall'iframe
    this._onMessageReady = (event) => {
      if (this._destroyed) return;
      if (event.origin !== this._targetOrigin) return;

      if (event.data && event.data.type === "iframe-ready") {
        this._pmReady = true;
        this._flushPmQueue();
      }
    };
    window.addEventListener("message", this._onMessageReady);

    // prima ping
    this._pingIframeReady();
  };

  // ping ricorsivo finché l'iframe non risponde ready, con un numero massimo di tentativi per evitare loop infiniti
  IframePlugin.prototype._pingIframeReady = function () {
    if (this._destroyed) return;
    if (!this.iframe?.contentWindow) return;

    // cancella eventuale ping già attivo
    if (this._pingTimer) {
      clearTimeout(this._pingTimer);
      this._pingTimer = null;
    }

    let attempts = 0;
    const maxAttempts = 20;

    const tick = () => {
      if (this._destroyed) return;
      if (this._pmReady) return;
      if (attempts++ >= maxAttempts) return;

      this.iframe.contentWindow.postMessage({ type: "ping-ready" }, this._targetOrigin);

      this._pingTimer = setTimeout(tick, 1000);
    };

    tick();
  };

  // invio messaggi in coda se iframe non pronto, altrimenti invio diretto
  IframePlugin.prototype._flushPmQueue = function () {
    if (this._destroyed) return;

    for (const msg of this._pmQueue.values()) {
      this.iframe.contentWindow.postMessage(msg, this._targetOrigin);
    }
    this._pmQueue.clear();
  };

  // invio messaggi all'iframe, con gestione coda se non pronto
  IframePlugin.prototype.postToIframe = function (msg) {
    if (!this.iframe?.contentWindow) return;
    if (this._destroyed) return;

    if (!this._pmReady) {
      const key = msg.type + ":" + (msg.payload?.tag ?? "");
      this._pmQueue.set(key, msg); // mantiene solo l’ultimo per tag
      return;
    }

    this.iframe.contentWindow.postMessage(msg, this._targetOrigin);
  };

  // pulizia completa del plugin, rimozione observer, listener e bottoni, da chiamare quando si vuole disattivare completamente il plugin o prima di rimuovere lo shadow host
  IframePlugin.prototype.destroy = function () {
    if (this._destroyed) return;
    this._destroyed = true;

    // stop ping loop e svuota coda
    this._stopPing();
    if (this._pmQueue) this._pmQueue.clear();

    // stop observer
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }

    // rimuovi listener window message (se li hai salvati come proprietà)
    if (this._onMessageMain) {
      window.removeEventListener("message", this._onMessageMain);
      this._onMessageMain = null;
    }
    if (this._onMessageReady) {
      window.removeEventListener("message", this._onMessageReady);
      this._onMessageReady = null;
    }

    // rimuovi listener iframe load (se salvato)
    if (this.iframe && this._onIframeLoad) {
      this.iframe.removeEventListener("load", this._onIframeLoad);
      this._onIframeLoad = null;
    }

    // rimuovi UI creata dal plugin
    try {
      deepQueryAll(document, '[data-btn-iframe="1"]').forEach((n) => n.remove());
    } catch (_) {}

    // pulizia mappa
    this.iconBtnMap.clear();

    // rimuovi modale dal shadow root
    if (this.shadowRoot) {
      const modal = this.shadowRoot.getElementById("exampleModalAngular");
      if (modal) modal.remove();
    }

    // rimuovi shadow host
    // if (this.shadowHost) this.shadowHost.remove();
  };

  // setter id museo
  IframePlugin.prototype.setIdMuseo = function (idMuseo) {
    this.idMuseo = idMuseo;
  };

  // setter token
  IframePlugin.prototype.setToken = function (token) {
    this.token = token;
  };

  // funzione helper per cambiare icona a rossa
  IframePlugin.prototype.changeRedButtonDefault = function (tag) {
    const iconBtn = this.iconBtnMap.get(tag);
    if (!iconBtn) return;
    iconBtn.src = this.options.pathIcon + "/icona_rossa.png";
    iconBtn.parentElement.title = "Incompleto";
  }

  // funzione helper per assicurarsi di avere un tag unico e persistente per ogni editor, con possibilità di definizione manuale tramite attributo o generazione automatica
  IframePlugin.prototype._ensureTag = function (el, prefix) {
    let tag = el.getAttribute("tag");
    if (tag && tag.trim()) return tag.trim();

    tag = el.getAttribute("data-iframe-auto-tag");
    if (!tag) {
      tag = `${prefix}_${++this._autoTagSeq}`;
      el.setAttribute("data-iframe-auto-tag", tag);
      el.setAttribute("tag", tag);
    }
    return tag;
  }

  // funzione helper per fermare il ping ricorsivo, utile in destroy o quando si vuole evitare ulteriori tentativi di comunicazione con l'iframe
  IframePlugin.prototype._stopPing = function () {
    if (this._pingTimer) {
      clearTimeout(this._pingTimer);
      this._pingTimer = null;
    }
  }

  // funzione helper per attaccare listener di input/change su un elemento o suoi contenteditable figli, con debounce e callback per estrazione testo, utile per aggiornare lo stato del bottone in base al contenuto dell'editor
  IframePlugin.prototype._attachStatusWatch = function (el, tag, getTextFn) {
    if (!el || !tag) return;
    
    // evita doppi bind
    if (el.getAttribute("data-iframe-watch") === "1") return;
    el.setAttribute("data-iframe-watch", "1");

    // debounce per non spammare status-button
    const schedule = () => {
      const text = getTextFn();
      const prev = this._lastStatusText.get(tag);
      if (text?.trim() === prev?.trim()) return;
      this._lastStatusText.set(tag, text);
      if (text.trim().length === 0) {
        this.changeRedButtonDefault(tag);
      } else if (this.options.autoCheckStatus) {
        this.checkStatus(tag, text);
      }
    };

    const t = (el.tagName || "").toLowerCase();
    if (t === "textarea" || t === "input" || t === "select") {
      el.addEventListener("change", schedule);
    }
    if (el.isContentEditable) {
      el.addEventListener("blur", schedule);
    } else {
      const ce = el.querySelector?.('[contenteditable="true"]');
      if (ce && ce.getAttribute("data-iframe-watch") !== "1") {
        ce.setAttribute("data-iframe-watch", "1");
        ce.addEventListener("blur", schedule);
      }
    }
    const quill = el.__quill || el.querySelector?.(".ql-editor")?.__quill || el.querySelector?.(".ql-container")?.__quill;
    if (quill && typeof quill.on === "function") {
      quill.on("text-change", schedule);
    }
  };

  // funzione helper per forzare refresh di tutti i bottoni in base al testo attuale degli editor/textarea
  IframePlugin.prototype.refreshAllTags = function () {
    const selector = 'textarea[editor="iframe"], div[editor="iframe"], input[editor="iframe"]';
  
    deepQueryAll(document, selector).forEach((el) => {
      const tag = el.getAttribute("tag") || el.getAttribute("data-iframe-auto-tag");
    
      if (!el.getAttribute("textarea-link") || !tag) return;
    
      console.log(`Refreshing in DOM element with tag=[${tag}]`);
      this.checkStatus(tag, this.extractTextAny(el));
    });
  };

  // funzione helper per querySelectorAll che scende anche dentro shadow DOM, utile per estrarre testo o trovare editor dinamici in qualsiasi punto del DOM
  function deepQueryAll(root, selector, out = []) {
    out.push(...root.querySelectorAll(selector));
    const walk = (node) => {
      node.querySelectorAll("*").forEach((el) => {
        if (el.shadowRoot) {
          out.push(...el.shadowRoot.querySelectorAll(selector));
          walk(el.shadowRoot);
        }
      });
    };
    walk(root);
    return out;
  }

  // funzione helper per creare un ID sicuro da qualsiasi stringa, utile per generare ID di elementi associati a tag dinamici senza rischiare caratteri non validi
  function _safeId(tag) {
    return (tag || "").toString().replace(/[^a-zA-Z0-9\-_:.]/g, "_");
  }

  global.IframePlugin = IframePlugin;
})(window);
