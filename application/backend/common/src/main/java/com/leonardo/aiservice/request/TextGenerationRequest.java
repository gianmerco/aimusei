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

    @Override
    public ImageContent getContent() {
        return content;
    }

}
