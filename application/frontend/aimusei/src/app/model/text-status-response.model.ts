export class TextStatusResponse {
  textStatus!: TextStatus;
  hashMatch: boolean = false;
}

export enum TextStatus {
  INCOMPLETO = "INCOMPLETO",
  GENERATO_AI = "GENERATO_AI",
  REVISIONATO = "REVISIONATO"
}
