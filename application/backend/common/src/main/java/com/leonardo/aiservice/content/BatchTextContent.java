package com.leonardo.aiservice.content;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

/**
 * un tipo di contenuto che aggrega più TextContent identificati da un ID arbitrario.
 * Utilizzato per operazioni batch, dove ogni entry rappresenta un testo da processare (o già processato)
 * e la chiave è un identificativo scelto dal chiamante per correlare input e output.
 */

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchTextContent implements Content {
    @Singular("entry")
    private final Map<String, TextContent> entries;

    public TextContent byId(String id) {
        return entries.get(id);
    }
}
