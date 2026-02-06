package it.prismaprogetti.aimusei.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaMassiveTexts {

	private String lang;
	private int processed;
	private int ok;
	private int failed;
	
}