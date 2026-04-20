package com.leonardo.aiservice.response;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Classe base per le risposte batch. Contiene lo stato del batch e l'eventuale ID del batch successivo.
 * Le sottoclassi definiscono il tipo di contenuto specifico.
 */

@Getter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class BatchResponse implements AiResponse {
    private final BatchStatus status;
    private final String nextBatchId;
}
