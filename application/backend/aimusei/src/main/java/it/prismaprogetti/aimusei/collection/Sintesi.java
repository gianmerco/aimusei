package it.prismaprogetti.aimusei.collection;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sintesi {

	private String generator;
	private String descrizioneAI;
	private String descrizioneReviewed;
	private boolean validata;
	private String validator;
	private LocalDateTime dataInsert; //system date
	private LocalDateTime dataValidation; //system date
	

	public String getLatestDescrizione() {
		return descrizioneReviewed == null ? descrizioneAI : descrizioneReviewed;
	}
	
	//TODO modifica nomi 
//	{
//		   public record SimplifiedText(
//		       @NotBlank String tipo,
//		       @NotBlank String text,
//		       @NotBlank String editor,
//		       @Min(1) int version
//		   )  
//		 
}
