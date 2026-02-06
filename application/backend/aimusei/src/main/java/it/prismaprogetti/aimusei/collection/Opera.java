package it.prismaprogetti.aimusei.collection;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import it.prismaprogetti.aimusei.model.StatoOpera;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "opera")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Opera {

	@Id
	private String id;
	@Field("nome")
	private String nome;
	private String descrizione;
	private int version; 
	private String hash;
	private String tag;
	private LocalDate lastUpdate;
	//TODO Rimuovuere le sintesi con suffisso _NEW
	private Sintesi sintesi;
	private StatoOpera statoOpera;
	private boolean latest;
	
	private String validator;
	
	private String engineLLM;
	
	
	
//	 @NotBlank
//	   String tag,     // e.g. "MUS-SEZ1-ITA"
//	 
//	   @Min(1)
//	  int vers,       // versione suggerita
//	 
//	  @NotBlank
//	  String contentText,  // testo originale
//	 
//	  @NotBlank
//	   String contentHash,  // hash per verifica
//	 
//	   @NotEmpty
//	   @Valid
//	   List<SimplifiedText> simplified

//	private Sintesi sintesi_dislessia;
//	private Sintesi sintesi_discalculia;
	@Override
	public String toString() {
		return "Opera [id=" + id + ", nome=" + nome + ", descrizione=" + descrizione + ", version=" + version
				+ ", hash=" + hash + ", tag=" + tag + ", lastUpdate=" + lastUpdate + ", sintesi=" + sintesi + "]";
	}
}
