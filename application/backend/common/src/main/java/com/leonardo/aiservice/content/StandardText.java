package com.leonardo.aiservice.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * una stringa di testo continua, appunto un paragrafo. Può rappresentare ad esempio una sezione di testo di un sito web
 */

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardText extends TextContent{

}
