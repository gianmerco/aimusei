(function (global) {
  function IframePlugin(options) {
    this.options = Object.assign(
      {
        pathIcon: "https://api-coll.museiitaliani.it/aimusei/api",
        urlIframe: "https://api-coll.museiitaliani.it/aimusei/",
        observerDom: false,
        token: '',
        idMuseo: ''
      },
      options || {}
    );

    this.iconBtnMap = new Map();
    this._ICONBTN_ = "iconBtn_";
    this.token = this.options.token;
    this.idMuseo = this.options.idMuseo;

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
  };

  // Gestione messaggi da iframe
  IframePlugin.prototype.listenMessages = function () {
    window.addEventListener("message", (event) => {
      if (
        this.options.urlIframe.indexOf(event.origin) == -1 ||
        event.data.type !== "saved"
      )
        return;

      console.log("Iframe response:", event.data.body);

      let elem = this.closeBtn;
      const parsedBody = JSON.parse(JSON.stringify(event.data.body));
      const iconBtn = this.iconBtnMap.get(parsedBody.tag);

      if (elem && iconBtn) {
        elem.click();
        iconBtn.src =
          this.options.pathIcon +
          "/icona_" +
          (event.data?.status === "ok"
            ? "verde"
            : event.data?.status === "ai"
            ? "blu"
            : "rossa") +
          ".png";
        let tooltip;
        if (["INFO_MUSEO", "ETR"].includes(parsedBody.context)) {
          tooltip =
            event.data?.status === "ok"
              ? "Easy to read revisionato"
              : event.data?.status === "ai"
              ? "Easy to read pronto"
              : "Easy to read incompleto";
        } else {
          tooltip =
            event.data?.status === "ok"
              ? "Revisionato"
              : event.data?.status === "ai"
              ? "Semplificato"
              : "Incompleto";
        }
        iconBtn.parentElement.title = tooltip;
      } else {
        console.log("Errore rif. icona non trovato " + iconBtn);
      }
    });
  };

  // Invio messaggi all’iframe
  IframePlugin.prototype.messageToIframe = function (tag, description, context, title) {
    const iframe = this.iframe;
    //const iframe = document.getElementById('widget-iframe');
    const iconBtn = this.iconBtnMap.get(tag);

    if (iframe && iconBtn) {
      iframe.contentWindow.postMessage(
        {
          type: "init",
          payload: {
            text: description,
            funz: "CREATE",
            tag: tag,
            context: context ? context : 'ETR', // verifica se deve impacchettare in json piu campi presi da tag child (indirizzo, categoria, textContent, etc)
            canGeneratePdf: context === "INFO_MUSEO",
            token: this.token,
            idMuseo: this.idMuseo,
            title: title ? title : ("Title " + tag),
            status: iconBtn.src.includes("rossa.png")
              ? "new"
              : iconBtn.src.includes("blu.png")
              ? "ai"
              : "verified",
          },
        },
        this.options.urlIframe
      );
    } else {
      console.log("Errore icona non trovata " + iconBtn);
    }
  };

  // Creazione iframe dinamico
  IframePlugin.prototype.createIframe = function () {
    if (!this.options.pathIcon) alert("ATTENTION: pathIcon is undefined");

    if (document.getElementById("widget-iframe")) return;

    const divContainer = document.createElement("div");

    divContainer.innerHTML = `<div class="modal fade" id="exampleModalAngular" style="opacity: 1;" tabindex="-1"
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
    this.iframe = this.shadowRoot.getElementById("widget-iframe");
    this.closeBtn = this.shadowRoot.getElementById("closeAngularModal");
    console.log("Iframe modal widget inserted", this.modal);

    setTimeout(() => {
      this.executeIframePluginTextArea();
      this.executeIframePluginDivInput();
      this.executeIframePluginForm();
    }, 500);
  };

  IframePlugin.prototype.checkStatus = function (tag, description) {
    const iframe = this.iframe;
    if (tag && description) {
      console.log("checkStatus called with tag:", tag, "description:", description);
      iframe.contentWindow.postMessage(
        {
          type: "status-button",
          payload: {
            text: description,
            tag: tag,
            token: this.token,
          }
        },
        this.options.urlIframe
      );
    }
  }

  IframePlugin.prototype.extractTextForm = function(form, payload, index) {
    form.querySelectorAll("input, textarea, select").forEach((input) => {
      // extractedText += `${input.name || input.id}: ${input.value}\n\n`;
      const key = input.name || input.id || `field_${index}`;
      if (input.type === "checkbox") {
        // Per checkbox: booleano singolo o array se più condividono lo stesso name
        if (
          input.name &&
          form.querySelectorAll(
            `input[type="checkbox"][name="${input.name}"]`
          ).length > 1
        ) {
          payload[input.name] = Array.from(
            form.querySelectorAll(
              `input[type="checkbox"][name="${input.name}"]`
            )
          )
            .filter((el) => el.checked)
            .map((el) => el.value);
        } else {
          payload[key] = input.checked;
        }
      } else if (input.type === "radio") {
        if (input.checked) {
          payload[key] = input.value;
        } else if (!(key in payload)) {
          // nessuna selezione registrata finora
          payload[key] = null;
        }
      } else if (input.tagName.toLowerCase() === "select") {
        if (input.multiple) {
          payload[key] = Array.from(input.selectedOptions).map(
            (o) => o.value
          );
        } else {
          payload[key] = input.value;
        }
      } else {
        payload[key] = input.value;
      }
    });
    return payload;
  } 

  IframePlugin.prototype.extractTextDiv = function(editor) {
    let paragraphs = null;
    let extractedText;
    if (editor.tagName.toLowerCase() === "input") {
      extractedText = editor.value;
    } else {
      // paragraphs = editor.querySelectorAll("p");
      paragraphs = editor.querySelectorAll(".ql-container.ql-snow");
      extractedText = Array.from(paragraphs)
        .map((p) => p.innerText.trim())
        .filter((t) => t.length > 0)
        .join("\n\n");
    }
    return extractedText;
  }

  IframePlugin.prototype.executeIframePluginTextArea = function () {
    console.log("Observer DOM scanning for textarea...");

    const textareas = document.querySelectorAll('textarea[editor="iframe"]');

    textareas.forEach((textarea, index) => {
      const tag = textarea.getAttribute("tag") || `AUTO_TAG_${index}`;
      const status = textarea.getAttribute("text-status") || "NEW";
      const id = textarea.id || `textarea-${index}`;
      const title = textarea.getAttribute("data-title");
      const context = textarea.getAttribute("context");

      if (textarea.getAttribute("textarea-link") != null)
        return;

      console.log(`Searching in DOM new textarea founded with id=[${id}] applyied pluginIframe`);

      const wrapper = document.createElement("div");
      wrapper.className = "d-flex align-items-center gap-3 mb-1";

      const button = document.createElement("button");
      button.type = "button";
      button.className = "btn btn-link";
      button.title = ["INFO_MUSEO", "ETR"].includes(context) ? 'Easy to read non pronto' : "Apri modale IFrame";
      //button.setAttribute('data-bs-toggle', 'modal');
      button.setAttribute("data-bs-target", "#exampleModalAngular");
      button.style.padding = "0";
      button.style.borderRadius = "20%";
      textarea.setAttribute("textarea-link", "exampleModalAngular");

      const icon = document.createElement("img");
      icon.id = this._ICONBTN_ + tag;
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

      // check status button
      this.checkStatus(tag, textarea.value);

      const iconColor = statusToColor[status] || "rossa";
      icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

      this.iconBtnMap.set(tag, icon);

      // click con arrow function mantiene il contesto this
      button.onclick = () => {
        window.currentTextArea = textarea;
        this.openModal();
        this.messageToIframe(tag, textarea.value, context, title);
      };

      button.appendChild(icon);
      wrapper.appendChild(button);
      textarea.parentNode.insertBefore(wrapper, textarea);
      console.log("added Iframe link by TAG:" + tag);
    });
  };

  IframePlugin.prototype.executeIframePluginDivInput = function () {
    console.log("Observer DOM scanning for tag div[editor] e input[editor]...");

    const editors = document.querySelectorAll(
      'div[editor="iframe"], input[editor="iframe"]'
    );

    editors.forEach((editor, index) => {
      const tag = editor.getAttribute("tag") || `AUTO_TAG_${index}`;
      const status = editor.getAttribute("text-status") || "NEW";
      const title = editor.getAttribute("data-title");
      const context = editor.getAttribute("context");

      // skip duplicates
      if (editor.getAttribute("textarea-link") != null)
        return;

      console.log(`Found editor [${tag}], attaching pluginIframe button`);

      // wrapper bottone
      const button = document.createElement("button");
      button.type = "button";
      button.className = "btn btn-link";
      button.title = "Apri modale IFrame";
      button.style.padding = "0";
      button.style.borderRadius = "20%";
      editor.setAttribute("textarea-link", "exampleModalAngular");

      const icon = document.createElement("img");
      icon.id = this._ICONBTN_ + tag;
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

      // check status button
      let extractedText = this.extractTextDiv(editor);
      this.checkStatus(tag, extractedText);

      const iconColor = statusToColor[status] || "rossa";
      icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

      this.iconBtnMap.set(tag, icon);

      button.onclick = () => {
        window.currentEditor = editor;
        extractedText = this.extractTextDiv(editor);
        console.log("Extracted text for iframe:", extractedText);
        this.openModal();
        this.messageToIframe(tag, extractedText, context, title);
      };

      button.appendChild(icon);
      let toolbar = null;
      if (editor.tagName.toLowerCase() === "input") {
        // find input wrapper item closest
        // toolbar = editor.closest('.v-input__control')?.querySelector('.v-input__slot');
        toolbar = editor.parentElement;
        console.log(`Matched wrapper [${tag}] for Input`, toolbar);
      } else {
        // find toolbar closest
        toolbar = editor
          .closest(".quillWrapper")
          ?.querySelector(".ql-toolbar.ql-snow");
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

        toolbar.appendChild(span);
        console.log(`Added button inside Quill toolbar for TAG: ${tag}`);
      } else {
        console.warn(`Nessuna toolbar Quill trovata per TAG: ${tag}`);
      }

      console.log(`added Iframe link for tag html: ${editor.tagName.toLowerCase()} by IFRAME-TAG: ${tag}`);
    });
  };

  IframePlugin.prototype.executeIframePluginForm = function () {
    console.log("Observer DOM scanning for form[editor]...");

    const forms = document.querySelectorAll('form[editor="iframe"]');

    forms.forEach((form, index) => {
      const tag = form.getAttribute("tag") || `AUTO_TAG_${index}`;
      const status = form.getAttribute("text-status") || "NEW";
      const title = form.getAttribute("data-title");

      // skip duplicates
      if (form.getAttribute("form-link") != null)
        return;

      console.log(`Found form [${tag}], attaching pluginIframe button`);

      // wrapper bottone
      const button = document.createElement("button");
      button.type = "button";
      button.className = "btn btn-link";
      button.title = 'Easy to read non pronto';
      button.style.padding = "0";
      button.style.borderRadius = "20%";
      form.setAttribute("form-link", "exampleModalAngular");

      const icon = document.createElement("img");
      icon.id = this._ICONBTN_ + tag;
      icon.alt = "Icona stato";
      icon.style.width = "2rem";
      icon.style.height = "2rem";
      icon.setAttribute("form-tag", this._ICONBTN_ + tag);

      const statusToColor = {
        NEW: "rossa",
        "GENERATED-AI": "blu",
        REVISIONED: "verde",
        NEW_VERSION: "rossa",
      };

      // check status button
      let payload = this.extractTextForm(form, {}, index);
      let extractedText = JSON.stringify(payload);
      this.checkStatus(tag, extractedText);

      const iconColor = statusToColor[status] || "rossa";
      icon.src = `${this.options.pathIcon}/icona_${iconColor}.png`;

      this.iconBtnMap.set(tag, icon);

      button.onclick = () => {
        window.currentEditor = form;
        payload = this.extractTextForm(form, {}, index);
        extractedText = JSON.stringify(payload);
        console.log("Extracted text for iframe:", extractedText);
        this.openModal();
        this.messageToIframe(tag, extractedText, "INFO_MUSEO", title);
      };

      button.appendChild(icon);
      let toolbar = null;
      if (form.tagName.toLowerCase() === "form") {
        //find input wrapper item closest
        toolbar = form.querySelector(".toolbar");
        console.log(`Matched wrapper [${tag}] for Input`, toolbar);
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

        toolbar.appendChild(span);
        console.log(`Added button inside Quill toolbar for TAG: ${tag}`);
      } else {
        console.warn(`Nessuna toolbar Quill trovata per TAG: ${tag}`);
      }

      console.log(`added Iframe link for tag html: ${form.tagName.toLowerCase()} by IFRAME-TAG: ${tag}`);
    });
  };

  IframePlugin.prototype.addObserverContentBody = function () {
    this.observer = new MutationObserver(() => {
      this.executeIframePluginTextArea();
      this.executeIframePluginDivInput();
      this.executeIframePluginForm();
      this.cleanupRemovedEditors();
    });

    //option default
    if (this.options.observerDom) this.startObserver();
  };

  IframePlugin.prototype.stopObserver = function () {
    if (this.observer) {
      this.observer.disconnect();
      console.log("IframePlugin: MutationObserver stopped successfully.");
    } else {
      console.warn("IframePlugin: Observer not available or already stopped.");
    }
  };

  IframePlugin.prototype.startObserver = function () {
    if (this.observer) {
      this.observer.observe(document.body, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ["style"],
      });
      console.log("IframePlugin: MutationObserver (re)started successfully.");
    } else {
      console.error(
        "IframePlugin: Cannot start, observer instance not found. Was the plugin initialized correctly?"
      );
    }
  };

  IframePlugin.prototype.cleanupRemovedEditors = function () {
    console.log("Cleaning orphaned iframe buttons...");

    const existingTags = Array.from(
      document.querySelectorAll(
        'div[editor="iframe"], input[editor="iframe"], textarea[editor="iframe"]'
      )
    )
      .map((el) => el.getAttribute("tag"))
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
            imgElement.closest("[data-btn-iframe]") || // caso v-tabs
            imgElement.closest("button")?.parentElement || // caso dentro un <span>
            imgElement.parentElement; // fallback finale

          if (buttonContainer && buttonContainer.parentElement) {
            buttonContainer.remove();
            console.log(`Removed DOM container for ${iconId}`);
          } else {
            console.warn(`No container found for ${iconId}`);
            imgElement.remove(); // fallback: elimina solo l’immagine
          }
        } else {
          console.warn(`?? Image not found for tag: ${tag}`);
        }
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
    });

    console.log(`Cleanup completed. Active buttons: ${this.iconBtnMap.size}`);
  };

  IframePlugin.prototype.openModal = function () {
    //const modal = this.shadowRoot.querySelector(".modal");
    const modal = this.modal;
    modal.classList.add("show");
    modal.style.display = "flex";
    modal.animate([{ opacity: 0 }, { opacity: 1 }], {
      duration: 300,
      fill: "forwards",
    });
  };

  IframePlugin.prototype.closeModal = function () {
    //const modal = this.shadowRoot.querySelector(".modal");
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

  IframePlugin.prototype.addNewTextAreaSection = function () {
    const container = document.querySelector("#dynamic-sections") || document.body;
    const section = document.createElement("div");
    section.className = "col-6 mt-4";

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
  };

  // Creazione host nel body per uso Shadow DOM
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

  // CSS Modal Style
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
