package it.prismaprogetti.aimusei.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MassiveTextsResponse {

	private List<TextHashedResponse> results;
	private MetaMassiveTexts meta;
}