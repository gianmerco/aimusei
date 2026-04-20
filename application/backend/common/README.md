# Libreria commons
Questa libreria contiene le classi DTO utilizzate per interagire con il back-end dell'AI gateway.

## Architettura

La libreria è organizzata in tre package principali:
- `com.leonardo.aiservice.request` - Richieste verso l'AI gateway
- `com.leonardo.aiservice.response` - Risposte dall'AI gateway
- `com.leonardo.aiservice.content` - Tipi di contenuto utilizzati da richieste e risposte

Tutte le classi utilizzano Lombok per la generazione automatica di builder, getter, equals/hashCode e toString. La serializzazione JSON è gestita tramite Jackson con supporto al polimorfismo.

## Richieste

Le richieste sono modellate tramite l'interfaccia `AiRequest`, che definisce il metodo `getContent()` per ottenere il contenuto della richiesta. Esistono tre implementazioni concrete:

### EtrRequest
Richiesta per semplificare un testo secondo le linee guida Easy-to-Read. Contiene un `TextContent` come input.

### PictogramsRequest
Richiesta per convertire un testo in pittogrammi ARASAAC. Contiene:
- `content`: il testo da convertire (`TextContent`)
- `wordsToPictograms`: una mappa di parole già associate ai rispettivi pittogrammi (`Map<String, ImageContent>`)

Si consiglia di utilizzare testo già semplificato (EtrText) per ottenere risultati migliori.

### TextGenerationRequest
Richiesta per generare una descrizione testuale a partire da un'immagine. Contiene:
- `content`: l'immagine da descrivere (`ImageContent`)
- `hint`: informazioni aggiuntive sull'immagine (es. nome dell'opera, museo) per migliorare la descrizione generata

## Risposte

Le risposte sono modellate tramite l'interfaccia `AiResponse`, che definisce il metodo `getContent()`. Esistono tre implementazioni concrete:

### TextResponse
Risposta contenente un singolo contenuto testuale (`TextContent`).

### ImageResponse
Risposta contenente una o più immagini (`ImageMapContent`). Utilizzata come risposta a `PictogramsRequest`.

### MultilingualTextResponse
Risposta contenente testo in più lingue (`MultilingualTextContent`).

## Contenuti

L'interfaccia `Content` rappresenta un generico tipo di contenuto. Le implementazioni sono organizzate gerarchicamente:

### TextContent (astratto)
Rappresenta un contenuto testuale con un campo `value` di tipo String. Sottotipi:
- **StandardText**: testo standard, ad esempio una sezione di un sito web
- **EtrText**: testo semplificato secondo le normative Easy-to-Read
- **JsonText**: testo formattato come JSON

### ImageContent (interfaccia)
Rappresenta un'immagine. Implementazioni:
- **Base64Image**: immagine codificata in base64 (campo `value` di tipo String)
- **ByteArrayImage**: immagine come array di byte (campo `value` di tipo byte[])

### MultilingualTextContent
Mappa che associa un codice lingua ISO 639-1 al corrispondente `TextContent`. Fornisce il metodo `ofLanguage(String lang)` per ottenere il testo in una specifica lingua.

### ImageMapContent
Lista di coppie parola-immagine (`List<Map.Entry<String, ImageContent>>`). Utilizzato per rappresentare il risultato di una richiesta di pittogrammi, mantenendo l'ordine delle parole nel testo originale e permettendo duplicati.

## Utilizzo

Tutte le classi concrete utilizzano il pattern Builder per la costruzione:

```java
// Creazione di una richiesta Easy-to-Read
EtrRequest request = EtrRequest.builder()
    .content(StandardText.builder()
        .value("Testo da semplificare")
        .build())
    .build();

// Creazione di una risposta testuale
TextResponse response = TextResponse.builder()
    .content(EtrText.builder()
        .value("Testo semplificato")
        .build())
    .build();
```