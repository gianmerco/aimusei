package com.leonardo.aiservice.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.leonardo.aiservice.content.Content;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * una generica risposta proveniente dall'aigateway. Varie risposte concrete esistono in base al data type del contenuto ritornato (es. String, Image, ...)
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "responseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextResponse.class, name = "text"),
    @JsonSubTypes.Type(value = ImageResponse.class, name = "image"),
    @JsonSubTypes.Type(value = MultilingualTextResponse.class, name = "multilingual")
})
public interface AiResponse {

    Content getContent();

}
