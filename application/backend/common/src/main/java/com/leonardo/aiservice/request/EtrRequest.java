package com.leonardo.aiservice.request;

import com.leonardo.aiservice.content.TextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * richiesta di semplificare un generico input testuale (TextualContent) secondo le linee guida easy-to-read
 */

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class EtrRequest implements AiRequest {
    private final TextContent content;

    @Override
    public TextContent getContent() {
        return content;
    }

}
