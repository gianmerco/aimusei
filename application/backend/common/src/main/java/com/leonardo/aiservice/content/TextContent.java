package com.leonardo.aiservice.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Rappresenta un generico contenuto di tipo testuale (ovvero rappresentabile in pratica tramite una String).
 * Il suo campo "value" è infatti una String.
 */

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "textType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StandardText.class, name = "standardText"),
    @JsonSubTypes.Type(value = JsonText.class, name = "jsonText"),
    @JsonSubTypes.Type(value = EtrText.class, name = "easyToReadText")
})
public abstract class TextContent implements Content {
    // ...
    private final String value;
}
