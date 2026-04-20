package com.leonardo.aiservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Stato di un batch, modellato sugli stati della OpenAI Batch API.
 */
@Getter
@AllArgsConstructor
public enum BatchStatus {
    VALIDATING("Il file di input è in fase di validazione prima che il batch possa iniziare"),
    FAILED("Il file di input non ha superato il processo di validazione"),
    IN_PROGRESS("Il file di input è stato validato con successo e il batch è attualmente in esecuzione"),
    FINALIZING("Il batch è stato completato e i risultati sono in fase di preparazione"),
    COMPLETED("Il batch è stato completato e i risultati sono pronti"),
    EXPIRED("Il batch non è stato completato entro la finestra temporale di 24 ore"),
    CANCELLING("Il batch è in fase di cancellazione (potrebbe richiedere fino a 10 minuti)"),
    CANCELLED("Il batch è stato cancellato");

    private final String description;
}
