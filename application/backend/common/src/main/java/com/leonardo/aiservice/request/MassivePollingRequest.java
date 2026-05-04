package com.leonardo.aiservice.request;

import com.leonardo.aiservice.content.TextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * richiesta di polling dello stato di un batch (in input ha solamente l'ID del batch)
 */

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MassivePollingRequest implements AiRequest {
    private final TextContent content;

    @Override
    public TextContent getContent() {
        return content;
    }
}
