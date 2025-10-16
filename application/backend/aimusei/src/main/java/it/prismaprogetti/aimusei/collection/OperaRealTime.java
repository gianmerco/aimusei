package it.prismaprogetti.aimusei.collection;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "opera_real_time")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperaRealTime {

	@Id
	private String id;
	@Field("nome")
	private String nome;
	private String descrizione;
	private List<Sintesi> sintesi;

//	private Sintesi sintesi_dislessia;
//	private Sintesi sintesi_discalculia;
}
