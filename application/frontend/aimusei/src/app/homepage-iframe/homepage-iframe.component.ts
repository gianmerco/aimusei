import { AccessibilityService } from './../services/accessibility.service';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { APP_ENVIRONMENT, SessionService } from '../services/session.service';
import { AppEnvironment } from '../model/env';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { takeWhile } from 'rxjs/operators';
import { GenerateRequestBody } from '../model/generate-request-body.model';
import { OriginalText } from '../model/original-text.model';
import { ValidateRequestBody } from '../model/validate-request-body.model';
import { ReviseRequestBody } from '../model/revise-request-body.model';
import { RegenerateRequestBody } from '../model/regenerate-request-body.model';
import { TextStatus } from '../model/text-status-response.model';

@Component({
  selector: 'app-homepage-iframe',
  templateUrl: './homepage-iframe.component.html',
  styleUrls: ['./homepage-iframe.component.scss'],
})
export class HomepageIframeComponent implements OnInit, OnDestroy {
  alive: boolean = true;
  form: FormGroup = new FormGroup({
    title: new FormControl(null, [
      Validators.required,
      Validators.minLength(1),
    ]),
    input: new FormControl(null, [
      Validators.required,
      Validators.minLength(1),
    ]),
    selectedType: new FormControl(null, []),
    version: new FormControl(null, []),
    tag: new FormControl({ value: null, disabled: true })
  });

  editApproval: boolean = false;
  oldNote: string = '';

  types: any[] = [];
  currentTag: string = '';
  selectedTypes: any[] = [];

  results: any = {};
  lastUpdates: any = {};
  editors: any = {};
  validators: any = {};
  validateObj: any = {};
  reqObj: any = {};
  edit: any = {};
  pdfExist: boolean = false;

  text: string = '';
  funz: string = '';
  status: string = '';
  hashCode: string = '';
  context: string = '';
  token: string = '';
  idMuseo: string = '';

  disabled: boolean = false;
  oldText: string = '';

  MOCKED_TYPE = 'EASY_TO_READ';

  constructor(
    @Inject(APP_ENVIRONMENT) public env: AppEnvironment,
    public sessionService: SessionService,
    private accessibilityService: AccessibilityService
  ) {}

  ngOnDestroy(): void {
    this.alive = false;
  }

  ngOnInit(): void {
    window.addEventListener('message', (event) => {
      if (event.data?.type === "ping-ready") {
        window.parent.postMessage({ type: "iframe-ready" }, '*');
        return;
      }

      const { payload } = event.data;
      if (!payload)
        return;

      console.log('Message received: ', event);

      this.setToken(payload?.token);

      if (event.data.type == 'status-button') {
        this.checkButtonStatus(payload.tag, payload.text);
        return ;
      }

      if (payload?.status && payload?.text && payload?.funz && payload?.tag) {
        this.status = payload.status;
        this.text = payload.text;
        this.funz = payload.funz;
        this.currentTag = payload.tag;
        this.idMuseo = payload.idMuseo;
        this.context = payload.context;

        this.form.get('title')?.setValue(payload.title);
        this.buildTypes();

        for (let t of this.types) {
          this.form.addControl('checkbox_' + t.key, new FormControl(false, []));
          this.form.addControl(t.key, new FormControl(null, []));
          this.form.addControl('approve_' + t.key, new FormControl(false, []));
          this.form.addControl('note_' + t.key, new FormControl(false, []));
          this.edit[t.key] = false;
          this.validateObj[t.key] = false;
          this.reqObj[t.key] = false;
        }
        this.search();
      } else {
        this.clearForm();
      }
    });
  }

  cancelUpdates(key: string) {
    this.edit[key] = false;
    this.form.get(key)?.setValue(this.oldText);
    this.oldText = '';
    if (this.editApproval) {
      this.editApproval = false;
      this.form.get('note_' + key)?.setValue(this.oldNote);
      this.oldNote = '';
    }
  }

  getResults() {
    return this.selectedTypes.sort((a, b) => {
      return (
        this.types.findIndex((x) => x.key == a.key) -
        this.types.findIndex((x) => x.key == b.key)
      );
    });
  }

  checkEdit(key: string) {
    return this.edit[key];
  }

  checkValidate(key: string, check: boolean = false) {
    if (check)
      return this.form.get(key)?.value?.length > 0;
    return this.validateObj[key];
  }

  revise(key: string) {
    this.validateObj[key] = true;
    const req: ReviseRequestBody = {
      sintesi: key,
      tag: this.currentTag,
      hash: this.hashCode,
      text: this.form.get(key)?.value,
    };
    this.accessibilityService
      .reviseText(req)
      .pipe(takeWhile(() => this.alive))
      .subscribe();
  }

  validate(key: string) {
    this.validateObj[key] = true;
    const req: ValidateRequestBody = {
      sintesi: key,
      tag: this.currentTag,
      hash: this.hashCode,
    };
    this.accessibilityService
      .validateText(req)
      .pipe(takeWhile(() => this.alive))
      .subscribe();
  }

  regenerate(key: string) {
    const req: RegenerateRequestBody = {
      sintesi: key,
      tag: this.currentTag,
      hash: this.hashCode
    };
    this.accessibilityService
      .regenerateSintesi(req)
      .pipe(takeWhile(() => this.alive))
      .subscribe((res) => this.form.get(key)?.patchValue(res));
  }

  insertNote(key: string) {
    this.edit[key] = false;
    this.editApproval = false;
    this.reqObj[key] = true;
    this.form.get('approve_' + key)?.disable();
  }

  insertETRText(key: string) {
    this.edit[key] = true;
    this.oldText = this.form.get(key)?.value;
    this.editApproval = true;
    this.oldNote = this.form.get('note_' + key)?.value;
    this.form.get('approve_' + key)?.enable();
  }

  formatDate(d: Date) {
    return d.toLocaleDateString();
  }

  salva() {
    let check = true;
    for (let t of this.types) {
      if (
        !this.form.get(t.key)?.value ||
        this.form.get(t.key)?.value.length == 0
      ) {
        check = false;
        break;
      }
    }
    let elems = document.getElementsByClassName('accordion-collapse');
    if (elems) {
      for (let i = 0; i < elems.length; i++) {
        elems.item(i)?.classList.remove('show');
      }
    }
    let elems2 = document.getElementsByClassName('accordion-button');
    if (elems2) {
      for (let i = 0; i < elems2.length; i++) {
        elems2.item(i)?.classList.add('collapsed');
      }
    }
    const status = check ? (this.types.some((t) => !this.validateObj[t.key]) ? (this.types.some((t) => this.reqObj[t.key]) ? 'req' : 'ai') : 'ok') : 'nok';
    console.log("status: ", status);
    window.parent.postMessage(
      {
        type: 'saved',
        id: 'xyz',
        body: {
          tag: this.currentTag,
          hash: this.hashCode, // HASH
          context: this.context
        },
        status: status,
      },
      '*'
    );
  }

  checkDisabled() {
    return this.types.some((t) => !this.form.get(t.key)?.value || this.form.get(t.key)?.value.length == 0);
  }

  getStatus(key: string) {
    if (this.form.get(key)?.value?.length > 0 && this.checkValidate(key)) {
      return 'Validato';
    } else if (this.form.get(key)?.value?.length > 0 && !this.checkValidate(key)) {
      return 'Generato';
    } else if (!this.checkValidate(key, true)) {
      return 'Non presente';
    } else {
      return '';
    }
  }

  formatLabel(type: string) {
    return type.replace(/\([^)]*\)/g, '').trim();
  }

  private search() {
    // mantiene il valore del tag attivo
    let tag = this.form.get('tag')?.value;
    let title = this.form.get('title')?.value + '';
    let version = (this.form.get('version')?.value ?? '') + '';
    this.selectedTypes = ([] as any[]).concat(this.types);
    this.form.reset();
    this.form.get('title')?.patchValue(title);
    this.form.get('version')?.patchValue(version);
    this.accessibilityService
      .getOriginalText(this.currentTag, this.text)
      .pipe(takeWhile(() => this.alive))
      .subscribe((res) => {
        const req: GenerateRequestBody = {
          tag: this.currentTag,
          originalText: this.text,
          title: title
        };
        if (this.context)
          req.context = this.context;
        if (res && res.textSimplified) {
          if (!res.hashMatch) {
            // CASISITICA 2
            this.accessibilityService
              .generateSimplifiedTexts(req)
              .pipe(takeWhile(() => this.alive))
              .subscribe((resp) => {
                this.updateView(resp);
                this.disabled = false;
              }, (error) => {
                console.error('Error generating simplified texts: ', error);
                this.form.get('input')?.setValue(this.text);
                this.form.get('version')?.setValue('');
                this.disabled = true;
              });
          } else {
            // CASISITICA 3
            // if (this.types.some(t => !res.textSimplified.includes(t)) || res.textSimplified.some(t => !t.text)) {
            if (!res.textSimplified.text) {
              this.accessibilityService
                .generateSimplifiedTexts(req)
                .pipe(takeWhile(() => this.alive))
                .subscribe((resp) => {
                  this.updateView(resp);
                  this.disabled = false;
                }, (error) => {
                  console.error('Error generating simplified texts: ', error);
                  this.form.get('input')?.setValue(this.text);
                  this.form.get('version')?.setValue('');
                  this.disabled = true;
                });
            } else {
              this.updateView(res);
              this.disabled = false;
            }
          }
        } else {
          // CASISITICA 1
          this.accessibilityService
            .generateSimplifiedTexts(req)
            .pipe(takeWhile(() => this.alive))
            .subscribe((resp) => {
              this.updateView(resp);
              this.disabled = false;
            }, (error) => {
              console.error('Error generating simplified texts: ', error);
              this.form.get('input')?.setValue(this.text);
              this.form.get('version')?.setValue('');
              this.disabled = true;
            });
        }
      }, (error) => {
        console.error('Error get simplified texts: ', error);
        this.form.get('input')?.setValue(this.text);
        this.form.get('version')?.setValue('');
        this.disabled = true;
      });
  }

  private updateView(res: OriginalText) {
    this.form.get('input')?.setValue(this.text);
    const date = res.textSimplified?.dateInsert ?? new Date();
    this.form.get('version')?.setValue(`V ${res.textVersion}.${new Date(date).toLocaleDateString()}`);
    setTimeout(() => {
      this.sessionService.showSpinner = true;
      setTimeout(() => {
        this.sessionService.showSpinner = false;
        this.hashCode = res.hashCode;
        if (res.textSimplified) {
          res.textSimplified.tipo = this.MOCKED_TYPE;
          this.results = {};
          this.lastUpdates = {};
          // for (let sint of res.textSimplified) {
            if (res.textSimplified.tipo) {
              if (this.selectedTypes.findIndex((x) => x.key == res.textSimplified.tipo) < 0) {
                this.form.get('checkbox_' + res.textSimplified.tipo)?.patchValue(true);
              }
              this.form.get(res.textSimplified.tipo)?.patchValue(res.textSimplified.text);
              this.results[res.textSimplified.tipo] = res.textSimplified.text;
              this.editors[res.textSimplified.tipo] = res.textSimplified.generator;
              this.validators[res.textSimplified.tipo] = res.textSimplified.validator;
              this.lastUpdates[res.textSimplified.tipo] = res.textSimplified.dateInsert
                ? new Date(res.textSimplified.dateInsert)
                : new Date();
              this.edit[res.textSimplified.tipo] = false;
              this.validateObj[res.textSimplified.tipo] = res.textSimplified.validate;
              // TODO rimuovere MOCK ↓
              this.form.get('note_' + res.textSimplified.tipo)?.patchValue(res.textSimplified.note ?? 'Protocollo AABB123');
              this.form.get('approve_' + res.textSimplified.tipo)?.patchValue(res.textSimplified.approve ?? true);
              this.form.get('approve_' + res.textSimplified.tipo)?.disable();
              this.reqObj[res.textSimplified.tipo] = !!res.textSimplified.note;
              // if (this.status == 'new') {
              //   this.setValidation(sint.tipo, false);
              // }
            }
          // }
        }
      }, 5000);
    }, 500);
  }

  private clearForm() {
    this.form.reset();
    this.currentTag = '';
    this.selectedTypes = [];
    this.results = {};
    this.lastUpdates = {};
    this.editors = {};
    this.validators = {};
    this.validateObj = {};
    this.reqObj = {};
    this.edit = {};
    this.text = '';
    this.funz = '';
    this.status = '';
    this.hashCode = '';
  }

  private checkButtonStatus(tag: string, description: string) {
    this.accessibilityService
      .getStatus(tag, description)
      .pipe(takeWhile(() => this.alive))
      .subscribe((resp) => {
        window.parent.postMessage(
          {
            type: 'check-status',
            id: 'xyz',
            body: {
              tag: tag,
            },
            status: resp?.textStatus == TextStatus.GENERATO_AI ? 'ai' :
              (resp?.textStatus == TextStatus.REVISIONATO ? 'ok' :
                (resp?.textStatus == TextStatus.REQUESTED? 'req' : 'nok')),
          },
          '*'
        );
      });
  }

  private buildTypes() {
    if (this.context == 'ETR') {
      this.types = [
        {
          label: 'Testo semplificato (Easy to read)',
          key: 'EASY_TO_READ',
        }
      ];
    } else {
      this.types = [
        {
          label: 'Testo semplificato (Easy to read)',
          key: 'EASY_TO_READ',
        },
        {
          label: 'Testo semplificato (Dislessia)',
          key: 'DISLESSIA',
        },
        {
          label: 'Supporto numerico semplificato (Discalculia)',
          key: 'DISCALCULIA',
        },
        {
          label: 'Testo semplificato (ADHD)',
          key: 'ADHD',
        },
        {
          label: 'Testo semplificato (CAA)',
          key: 'CAA',
        },
      ];
    }
  }

  private setToken(token: string | null) {
    if (token) {
      this.token = token;
      this.accessibilityService.setHeaders(this.token);
    } else {
      this.token = '';
      this.accessibilityService.setHeaders(null);
    }
  }
}
