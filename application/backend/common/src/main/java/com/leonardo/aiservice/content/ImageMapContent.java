package com.leonardo.aiservice.content;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AccessLevel;

/**
 * rappresenta una mappa che associa ad una stringa un'immagine. 
 * E' utilizzato quindi per ritornare il risultato di una PictogramsRequest, dove le chiavi sono parole (anche più di una)
 * del contenuto della richiesta, ed è associata l'immagine corrispondente.
 * L'implementazione della mappa è una Lista di map entries, che permette di iterare mantenendo l'ordine che le parole hanno nel testo originale e implementando l'associazione parole-immagine, permettendo allo stesso tempo i duplicati
 */

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageMapContent implements Content {
    private final List<Map.Entry<String, ImageContent>> value;

}
