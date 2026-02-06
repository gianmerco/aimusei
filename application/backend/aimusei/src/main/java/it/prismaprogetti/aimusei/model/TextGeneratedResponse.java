package it.prismaprogetti.aimusei.model;

import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.Sintesi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextGeneratedResponse {

	private String engineLLM;
	private int textVersion;
	private String hashCode;
	private String title;
	private SimplifiedText textSimplified;
	private boolean generated;

	public static TextGeneratedResponse fromOpera(Opera opera) {

		Sintesi sintesi = opera.getSintesi();

		TextGeneratedResponseBuilder builder = TextGeneratedResponse.builder()
				.engineLLM(opera.getEngineLLM())
				.textVersion(opera.getVersion())
				.hashCode(opera.getHash())
				.title(opera.getNome());

		if (sintesi != null) {
			builder
			.textSimplified(new SimplifiedText(
					sintesi.getLatestDescrizione(),
					sintesi.getGenerator(), sintesi.getValidator(), sintesi.getDataInsert(),
					sintesi.getDataValidation(), sintesi.isValidata()))
			.generated(true);
		}
		return builder.build();
	}
}