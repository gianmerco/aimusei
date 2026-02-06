package com.leonardo.aiservice.response;

import com.leonardo.aiservice.content.MultilingualTextContent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * risposta contenente una risposta testuale per ciascuna lingua in un set di lingue determinato dal server
*/

@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MultilingualTextResponse implements AiResponse{
    private final MultilingualTextContent content;

    @Override
    public MultilingualTextContent getContent() {
        return content;
    }
}
