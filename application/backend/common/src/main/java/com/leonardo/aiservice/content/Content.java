package com.leonardo.aiservice.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * un generico tipo da passare all'interno di richieste (sottoclassi di AiRequest), o da ricevere all'interno di risposte (sottoclassi di AiResponse)
 */


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "contentType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextContent.class, name = "text"),
    @JsonSubTypes.Type(value = ImageContent.class, name = "image"),
    @JsonSubTypes.Type(value = MultilingualTextContent.class, name = "multilingualText"),
    @JsonSubTypes.Type(value = ImageMapContent.class, name = "imageList")
})
public interface Content {
    // ...
}
