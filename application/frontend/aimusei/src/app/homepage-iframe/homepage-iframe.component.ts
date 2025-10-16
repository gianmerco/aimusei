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
    version: new FormControl('V4', []),
    tag: new FormControl({ value: null, disabled: true }),
  });

  types: any[] = [
    {
      label: 'Testo facilitato (Easy to read)',
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
  currentTag: string = '';
  selectedTypes: any[] = [];

  results: any = {};
  lastUpdates: any = {};
  editors: any = {};
  validators: any = {};
  validateObj: any = {};
  edit: any = {};

  text: string = '';
  funz: string = '';
  tag: string = '';
  status: string = '';
  hashCode: string = '';

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
      const { payload } = event.data;
      if (!payload) return;
      console.log('Message received: ', event);
      if (payload?.text && payload?.funz && payload?.tag && payload?.status) {
        this.status = payload.status;
        this.text = payload.text;
        this.funz = payload.funz;
        this.form.get('title')?.setValue(payload.title);
        this.currentTag = payload.tag;
        for (let t of this.types) {
          this.form.addControl('checkbox_' + t.key, new FormControl(false, []));
          this.form.addControl(t.key, new FormControl(null, []));
          this.edit[t.key] = false;
          this.validateObj[t.key] = false;
        }
        this.search();
      } else {
        this.clearForm();
      }
    });
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
    if (check) {
      return this.form.get(key)?.value?.length > 0;
    }
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
    // this.apiService.cambiaValiditaSintesi(this.currentTag, this.validateObj[key], key).pipe(takeWhile(()=>this.alive)).subscribe();
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
    // this.apiService.getSintesi(this.currentTag, key + '_NEW').pipe(takeWhile(() => this.alive)).subscribe((res: any) => {
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

  formatDate(d: Date) {
    return d.toLocaleDateString(); //+' - '+d.toLocaleTimeString();
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
    window.parent.postMessage(
      {
        type: 'saved',
        id: 'xyz',
        body: {
          tag: this.currentTag,
          hash: this.hashCode, // HASH
        },
        status: check
          ? this.types.some((t) => !this.validateObj[t.key])
            ? 'ai'
            : 'ok'
          : 'nok',
      },
      '*'
    );
  }

  checkDisabled() {
    return this.types.some(
      (t) =>
        !this.form.get(t.key)?.value || this.form.get(t.key)?.value.length == 0
    );
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

  private search() {
    // mantiene il valore del tag attivo
    let tag = this.form.get('tag')?.value;
    let title = this.form.get('title')?.value + '';
    let version = this.form.get('version')?.value + '';
    this.selectedTypes = ([] as any[]).concat(this.types);
    this.form.reset();
    this.form.get('title')?.patchValue(title);
    this.form.get('version')?.patchValue(version);
    // this.apiService.getOpera(this.currentTag).pipe(takeWhile(()=>this.alive)).subscribe(res=>{
    this.accessibilityService
      .getOriginalText(
        this.currentTag,
        this.text
      )
      .pipe(takeWhile(() => this.alive))
      .subscribe((res) => {
        const req: GenerateRequestBody = {
          tag: this.currentTag,
          originalText: this.text,
          title: title,
        };
        if (res && res.textSimplified) {
          if (!res.hashMatch) {
            // CASISITICA 2
            this.accessibilityService
              .generateSimplifiedTexts(req)
              .pipe(takeWhile(() => this.alive))
              .subscribe((resp) => {
                this.updateView(resp);
              });
          } else {
            // CASISITICA 3
            if (this.types.some(t => !res.textSimplified.includes(t)) || res.textSimplified.some(t => !t.text)) {
              this.accessibilityService
                .generateSimplifiedTexts(req)
                .pipe(takeWhile(() => this.alive))
                .subscribe((resp) => {
                  this.updateView(resp);
                });
            } else {
              this.updateView(res);
            }
          }
        } else {
          // CASISITICA 1
          this.accessibilityService
            .generateSimplifiedTexts(req)
            .pipe(takeWhile(() => this.alive))
            .subscribe((resp) => {
              this.updateView(resp);
            });
        }
      });
  }

  private updateView(res: OriginalText) {
    this.form.get('input')?.setValue(this.text);
    setTimeout(() => {
      this.sessionService.showSpinner = true;
      setTimeout(() => {
        this.sessionService.showSpinner = false;
        this.hashCode = res.hashCode;
        if (res.textSimplified) {
          this.results = {};
          this.lastUpdates = {};
          for (let sint of res.textSimplified) {
            if (sint.tipo) {
              if (this.selectedTypes.findIndex((x) => x.key == sint.tipo) < 0) {
                this.form.get('checkbox_' + sint.tipo)?.patchValue(true);
              }
              this.form.get(sint.tipo)?.patchValue(sint.text);
              this.results[sint.tipo] = sint.text;
              this.editors[sint.tipo] = sint.generator;
              this.validators[sint.tipo] = sint.validator;
              this.lastUpdates[sint.tipo] = sint.dateInsert
                ? new Date(sint.dateInsert)
                : new Date();
              this.edit[sint.tipo] = false;
              this.validateObj[sint.tipo] = sint.validate;
              // if (this.status == 'new') {
              //   this.setValidation(sint.tipo, false);
              // }
            }
          }
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
    this.edit = {};
    this.text = '';
    this.funz = '';
    this.tag = '';
    this.status = '';
    this.hashCode = '';
  }
}
