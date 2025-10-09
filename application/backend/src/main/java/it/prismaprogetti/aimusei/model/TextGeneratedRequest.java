package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class TextGeneratedRequest {

	private String tag;     
	private String originalText;
	private String title;
}
