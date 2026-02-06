import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { APP_ENVIRONMENT } from './session.service';
import { AppEnvironment } from '../model/env';
import { OriginalText } from '../model/original-text.model';
import { GenerateRequestBody } from '../model/generate-request-body.model';
import { ValidateRequestBody } from '../model/validate-request-body.model';
import { ReviseRequestBody } from '../model/revise-request-body.model';
import { Constants } from '../constants/constants';
import { RegenerateRequestBody } from '../model/regenerate-request-body.model';

@Injectable({
  providedIn: 'root',
})
export class AccessibilityService {
  private basePath: string = Constants.accessibility_reqwuest_mapping;

  constructor(
    private http: HttpClient,
    @Inject(APP_ENVIRONMENT) public env: AppEnvironment
  ) {}

  getOriginalText(tag: string, originalText: string) {
    let params: HttpParams = new HttpParams();
    params = params.set('tag', tag);
    params = params.set('originalText', originalText);
    return this.http.get<OriginalText>(this.env.apiUrl + this.basePath + '/getOriginalText', { params });
  }

  getTextStatus(tag: string) {
    let headers = new HttpHeaders();
    headers = headers.set('Authorization', '');
    return this.http.get<any>(this.env.apiUrl + this.basePath + '/status' + `/${tag}`, { headers });
  }

  generateSimplifiedTexts(body: GenerateRequestBody) {
    return this.http.post<OriginalText>(this.env.apiUrl + this.basePath + '/generate', body);
  }

  validateText(body: ValidateRequestBody) {
    return this.http.patch(this.env.apiUrl + this.basePath + '/validate', body);
  }

  regenerateSintesi(body: RegenerateRequestBody) {
    return this.http.patch(this.env.apiUrl + this.basePath + '/regenerateSintesi', body, { responseType: 'text' });
  }

  reviseText(body: ReviseRequestBody) {
    return this.http.put(this.env.apiUrl + this.basePath + '/revise', body);
  }

  generatePdf() {
    return this.http.post(this.env.apiUrl + this.basePath + '/generateImage', {}, { responseType: 'blob' });
  }
}
