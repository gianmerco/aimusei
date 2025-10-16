package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class ModificaFlagSintesiRequest {

	private String tag;
	private boolean validata;
	private Disabilita disabilita;

}
