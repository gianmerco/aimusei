package com.leonardo.aiservice.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.leonardo.aiservice.content.Content;

/**
 * una generica richiesta all'AI gateway. Le classi che implementano questa interfaccia rappresentano una richiesta per una specifica operazione su un
 * determinato input
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "requestType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EtrRequest.class, name = "etr"),
    @JsonSubTypes.Type(value = PictogramsRequest.class, name = "pictograms"),
    @JsonSubTypes.Type(value = TextGenerationRequest.class, name = "textGeneration"),
    @JsonSubTypes.Type(value = EtrMassiveRequest.class, name = "etrMassive")
})
public interface AiRequest {
    // ...
    Content getContent();
}
