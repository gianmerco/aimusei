package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class ModificaSintesiRequest {

	private String tag;
	private String nuovaSintesi;
	private TipoDisabilita disabilita;

}
