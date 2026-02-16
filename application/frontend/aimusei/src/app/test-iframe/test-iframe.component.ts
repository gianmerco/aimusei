import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-test-iframe',
  templateUrl: './test-iframe.component.html',
  styleUrls: ['./test-iframe.component.css'],
})
export class TestIframeComponent implements OnInit {

  ngOnInit(): void {
    const tag = 'MUS1-SEZ1-ITA';
    const context = 'ETR';
    const canGeneratePdf = false;
    const iconBtn = 'rossa.png';
    const token = 'JWT';
    const idMuseo = '1';
    console.log("URL AMBIENTE: ", window.location.href);
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
          token: token,
          idMuseo: idMuseo,
          title: "Title " + tag,
          status: iconBtn.includes("rossa.png")
            ? "new"
            : iconBtn.includes("blu.png")
            ? "ai"
            : "verified",
        },
      },
      window.location.href
    );
  }
}
