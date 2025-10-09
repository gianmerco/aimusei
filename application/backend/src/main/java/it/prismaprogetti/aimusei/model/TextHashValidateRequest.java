package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class TextHashValidateRequest extends HashValidateRequest {
	private String text;
}
