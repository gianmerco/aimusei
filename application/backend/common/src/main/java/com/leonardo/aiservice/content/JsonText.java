package com.leonardo.aiservice.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


/**
 * Una stringa formattata come un Json
 */

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonText extends TextContent{

}
