package com.leonardo.aiservice.response;

import com.leonardo.aiservice.content.BatchTextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * risposta contenente più testi processati in batch, ciascuno identificato dallo stesso ID usato nella richiesta corrispondente
 */

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class BatchTextResponse implements AiResponse {
    private final BatchTextContent content;

    @Override
    public BatchTextContent getContent() {
        return content;
    }
}
