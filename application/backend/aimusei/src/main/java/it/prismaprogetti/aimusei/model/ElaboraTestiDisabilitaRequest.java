package it.prismaprogetti.aimusei.model;

import java.util.Set;

import lombok.Data;

@Data
public class ElaboraTestiDisabilitaRequest {

	private String titolo;
	private String descrizione;
	private String tag;
	private Set<Disabilita> disabilita;
}