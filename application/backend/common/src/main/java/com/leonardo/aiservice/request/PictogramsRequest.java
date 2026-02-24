package com.leonardo.aiservice.request;
import java.util.Map;

import com.leonardo.aiservice.content.ImageContent;
import com.leonardo.aiservice.content.TextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

/**
 * richiesta di convertire un generico input testuale (TextualContent) in una lista di pittogrammi ARASAAC.
 * Tuttavia, in pratica, questa classe dovrebbe utilizzare soltando EasyToReadText, perché l'operazione sarebbe molto costosa
 * (e di risultato non buono) per un testo non semplificato
 */

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PictogramsRequest implements AiRequest {
    private final TextContent content;
    @Singular("wordToPictogram")
    private final Map<String, ImageContent> wordsToPictograms; // TODO magari un tipo dedicato?

    @Override
    public TextContent getContent() {
        return content;
    }

    public Map<String, ImageContent> getWordsToPictograms() {
        return wordsToPictograms;
    }

}
