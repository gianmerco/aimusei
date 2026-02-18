import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { APP_ENVIRONMENT } from './session.service';
import { AppEnvironment } from '../model/env';
import { OriginalText } from '../model/original-text.model';
import { GenerateRequestBody } from '../model/generate-request-body.model';
import { ValidateRequestBody } from '../model/validate-request-body.model';
import { ReviseRequestBody } from '../model/revise-request-body.model';
import { Constants } from '../constants/constants';
import { RegenerateRequestBody } from '../model/regenerate-request-body.model';
import { TextStatusResponse } from '../model/text-status-response.model';

@Injectable({
  providedIn: 'root',
})
export class AccessibilityService {
  private basePath: string = Constants.accessibility_reqwuest_mapping;

  constructor(
    private http: HttpClient,
    @Inject(APP_ENVIRONMENT) public env: AppEnvironment
  ) {}

  /**
   * check if original text already exists in db
   * @param tag
   * @param originalText
   * @returns object
   */
  getOriginalText(tag: string, originalText: string) {
    let params: HttpParams = new HttpParams();
    params = params.set('tag', tag);
    params = params.set('originalText', originalText);
    return this.http.get<OriginalText>(this.env.apiUrl + this.basePath + '/getOriginalText', { params });
  }

  // getTextStatus(tag: string) {
  //   let headers = new HttpHeaders();
  //   headers = headers.set('Authorization', '');
  //   return this.http.get<any>(this.env.apiUrl + this.basePath + '/status' + `/${tag}`, { headers });
  // }

  /**
   * generation of simplified texts
   * @param body
   * @returns object
   */
  generateSimplifiedTexts(body: GenerateRequestBody) {
    return this.http.post<OriginalText>(this.env.apiUrl + this.basePath + '/generate', body);
  }

  /**
   * validation of text (unused)
   * @param body
   * @returns void
   */
  validateText(body: ValidateRequestBody) {
    return this.http.patch(this.env.apiUrl + this.basePath + '/validate', body);
  }

  /**
   * regenerate simplified text
   * @param body
   * @returns string
   */
  regenerateSintesi(body: RegenerateRequestBody) {
    return this.http.patch(this.env.apiUrl + this.basePath + '/regenerateSintesi', body, { responseType: 'text' });
  }

  /**
   * revision and validation of text
   * @param body
   * @returns void
   */
  reviseText(body: ReviseRequestBody) {
    return this.http.put(this.env.apiUrl + this.basePath + '/revise', body);
  }

  /**
   * generate aarasac pdf
   * @returns byte[]
   */
  generatePdf(tag: string, idMuseo: string) {
    return this.http.post(this.env.apiUrl + this.basePath + '/generateImage', { tag, idMuseo }, { responseType: 'blob' });
  }

  /**
   * check status of an iframe button
   * @param tag
   * @param originalText
   * @returns object
   */
  getStatus(tag: string, originalText: string) {
    let params: HttpParams = new HttpParams();
    params = params.set('tag', tag);
    params = params.set('originalText', originalText);
    return this.http.get<TextStatusResponse>(this.env.apiUrl + this.basePath + '/getStatus', { params });
  }

  /**
   * get temporary bucket s3 url
   * @param idMuseo
   * @returns string
   */
	getImageUrl(idMuseo: string) {
    let params: HttpParams = new HttpParams();
    params = params.set('idMuseo', idMuseo);
    return this.http.get<any>(this.env.apiUrl + this.basePath + '/getImageUrl', { params });
	}

  /**
   * get aarasac pdf
   * @param idMuseo
   * @returns byte[]
   */
  getImage(idMuseo: string) {
    let params: HttpParams = new HttpParams();
    params = params.set('idMuseo', idMuseo);
    return this.http.get(this.env.apiUrl + this.basePath + '/getImage', { params, responseType: 'blob' });
	}

  /**
   * check if pdf already exists
   * @param idMuseo
   * @returns boolean
   */
	imageExists(idMuseo: string) {
    let params: HttpParams = new HttpParams();
    params = params.set('idMuseo', idMuseo);
    return this.http.get<boolean>(this.env.apiUrl + this.basePath + '/imageExists', { params });
	}
}
