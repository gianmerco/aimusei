package com.leonardo.aiservice.request;

import com.leonardo.aiservice.content.ImageContent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * richiesta di generare una descrizione testuale a partire da un'immagine
 */

@Builder
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TextGenerationRequest implements AiRequest{
    private final ImageContent content;

    /* questo sarebbe qualsiasi informazione sul contenuto dell'immagine che può aiutare l'AI a generare una descrizione. Si consiglia fortemente di utilizzarlo. Può anche essere semplicemente il nome dell'opera o del museo, o anche l'alt-text statico originario */
    private final String hint;

    @Override
    public ImageContent getContent() {
        return content;
    }

}
