package it.prismaprogetti.aimusei.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextHashedResponse {

	private String tag;
	private String hash;
	private String validator;
	private Status status; // OK, MISMATCH, NOT_FOUND, ERROR
	private String textGeneratedAI;
	private String textRevisioned;
	
	
	public enum Status {
		OK, MISMATCH, NOT_FOUND, ERROR
	}
}
