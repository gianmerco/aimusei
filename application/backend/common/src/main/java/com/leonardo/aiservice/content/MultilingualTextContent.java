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
 * un tipo di contenuto che aggrega TextContent, ma su più lingue. Il contenuto è una mappa che associa ad una lingua (rappresentata dal suo codice ISO 639-1) alla risposta in quella lingua
 * TODO: aggiungi un enum per le lingue
*/

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultilingualTextContent implements Content {
    // TODO magari dare un nome un po' più generico ("multiple"...)
    @Singular("singleLanguageString")
    private final Map<String, TextContent> value;

    public TextContent ofLanguage(String lang) {
        return value.get(lang);
    }
}
