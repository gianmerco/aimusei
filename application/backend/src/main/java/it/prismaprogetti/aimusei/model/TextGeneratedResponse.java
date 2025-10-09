package it.prismaprogetti.aimusei.model;

import java.util.List;

import it.prismaprogetti.aimusei.collection.Opera;
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
	private List<SimplifiedText> textSimplified;
	
	
	public static TextGeneratedResponse fromOpera(Opera opera) {
		
		return TextGeneratedResponse.builder()
		.engineLLM(opera.getEngineLLM())
		.textVersion(opera.getVersion())
		.hashCode(opera.getHash())
		.textSimplified(opera.getSintesi().stream().map(s ->
			new SimplifiedText(s.getDisabilita(), s.getDescrizioneReviewed()==null?s.getDescrizioneAI():s.getDescrizioneReviewed(), s.getGenerator(), s.getValidator(), s.getDataInsert(), s.getDataValidation(),s.isValidata())
		).toList())
		.title(opera.getNome())
		.build();
		
	}

}
