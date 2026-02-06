package com.leonardo.aiservice.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "imageType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Base64Image.class, name = "base64Image"),
    @JsonSubTypes.Type(value = ByteArrayImage.class, name = "byteImage")
})
public interface ImageContent extends Content {
    // ...
}
