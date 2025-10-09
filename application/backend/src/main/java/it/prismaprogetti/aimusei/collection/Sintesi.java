package it.prismaprogetti.aimusei.collection;

import java.time.LocalDateTime;

import it.prismaprogetti.aimusei.model.TipoDisabilita;
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
	private TipoDisabilita disabilita;
	private String descrizioneAI;
	private String descrizioneReviewed;
	private boolean validata;
	private String validator;
	private LocalDateTime dataInsert; //system date
	private LocalDateTime dataValidation; //system date
	

	
	
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
