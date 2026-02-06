package com.leonardo.aiservice.response;

import com.leonardo.aiservice.content.TextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * una risposta dell'ai gateway che consiste in una singola stringa di testo
 */

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TextResponse implements AiResponse {
    // ...
    private final TextContent content;

    @Override
    public TextContent getContent() {
        return content;
    }

}
