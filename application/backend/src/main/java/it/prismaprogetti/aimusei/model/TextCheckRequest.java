package it.prismaprogetti.aimusei.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextCheckRequest {

	private String tag;
	private String originalText;
	
}