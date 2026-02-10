import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-test-iframe',
  templateUrl: './test-iframe.component.html',
  styleUrls: ['./test-iframe.component.css'],
})
export class TestIframeComponent implements OnInit {
  urlIframe: string = "https://api-coll.museiitaliani.it/aimusei/api";

  ngOnInit(): void {
    const tag = 'MUS1-SEZ1-ITA';
    const context = 'ETR';
    const canGeneratePdf = false;
    const iconBtn = 'rossa.png';
    //@ts-ignore
    window.postMessage(
      {
        type: "init",
        payload: {
          text: 'Test',
          funz: "CREATE",
          tag: tag,
          context: context, // verifica se deve impacchettare in json piu campi presi da tag child (indirizzo, categoria, textContent, etc)
          canGeneratePdf: canGeneratePdf,
          // token: this.token,
          title: "Title " + tag,
          status: iconBtn.includes("rossa.png")
            ? "new"
            : iconBtn.includes("blu.png")
            ? "ai"
            : "verified",
        },
      },
      this.urlIframe
    );
  }
}
