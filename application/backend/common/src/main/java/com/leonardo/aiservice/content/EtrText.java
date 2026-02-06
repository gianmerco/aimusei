package com.leonardo.aiservice.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * una stringa continua (come un @StandardText), ma semplificata dal servizio AI e secondo le normative Easy-to-Read
 */

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtrText extends TextContent {

}
