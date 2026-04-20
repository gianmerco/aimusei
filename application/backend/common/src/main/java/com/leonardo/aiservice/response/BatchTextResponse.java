package com.leonardo.aiservice.response;

import com.leonardo.aiservice.content.BatchTextContent;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * risposta contenente lo stato di un batch e, se completato e ultimo della serie, il contenuto testuale risultante.
 */

@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class BatchTextResponse extends BatchResponse {
    private final BatchTextContent content;

    @Override
    public BatchTextContent getContent() {
        return content;
    }
}
