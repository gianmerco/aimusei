package com.leonardo.aiservice.content;

import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import lombok.AccessLevel;

/**
 * rappresenta una mappa che associa ad una stringa un'immagine. 
 * E' utilizzato quindi per ritornare il risultato di una PictogramsRequest, dove le chiavi sono parole (anche più di una)
 * del contenuto della richiesta, ed è associata l'immagine corrispondente.
 * L'implementazione della mappa è una LinkedHashMap, che permette di iterare sulla mappa mantenendo l'ordine che le parole hanno
 * nel testo originale
 */

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageMapContent implements Content {
    private final LinkedHashMap<String, ImageContent> value;

}
