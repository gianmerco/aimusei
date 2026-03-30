package com.leonardo.aiservice.request;

import com.leonardo.aiservice.content.ImageContent;
import com.leonardo.aiservice.content.TextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * richiesta di convertire un generico input testuale (TextualContent) in una lista di pittogrammi ARASAAC.
 * Tuttavia, in pratica, questa classe dovrebbe utilizzare soltando EasyToReadText, perché l'operazione sarebbe molto costosa
 * (e di risultato non buono) per un testo non semplificato.
 * In input è da inserire il testo da semplificare e l'immagine del museo di riferimento, in modo che la si possa usare insieme agli altri pittogrammi
 */

@Builder
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PictogramsRequest implements AiRequest {
    private final TextContent content;
    private final ImageContent museumImage;

    @Override
    public TextContent getContent() {
        return content;
    }

}
