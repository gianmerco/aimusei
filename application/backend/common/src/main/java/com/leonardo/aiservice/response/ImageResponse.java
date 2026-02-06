package com.leonardo.aiservice.response;

import com.leonardo.aiservice.content.ImageListContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * una risposta contenente una o più immagini
 */

@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ImageResponse implements AiResponse {
    // ...
    private final ImageListContent content;

    @Override
    public ImageListContent getContent() {
        return content;
    }

}
