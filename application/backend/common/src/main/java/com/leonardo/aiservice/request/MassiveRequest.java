package com.leonardo.aiservice.request;

/**
 * Marker interface per richieste batch. Permette ai consumer di distinguere
 * richieste singole da richieste massive tramite instanceof.
 */
public interface MassiveRequest extends AiRequest {
}
