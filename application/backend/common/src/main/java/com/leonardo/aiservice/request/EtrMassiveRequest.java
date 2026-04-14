package com.leonardo.aiservice.request;

import com.leonardo.aiservice.content.BatchTextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * richiesta di semplificare in modalità batch più testi secondo le linee guida easy-to-read.
 * Ogni testo è identificato da un ID arbitrario, utile per correlare input e output.
 */

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class EtrMassiveRequest implements MassiveRequest {
    private final BatchTextContent content;

    @Override
    public BatchTextContent getContent() {
        return content;
    }
}
