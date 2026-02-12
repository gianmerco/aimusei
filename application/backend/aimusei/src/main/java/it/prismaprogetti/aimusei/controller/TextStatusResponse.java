package it.prismaprogetti.aimusei.controller;

import it.prismaprogetti.aimusei.model.StatoOpera;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextStatusResponse {

	private StatoOpera textStatus;
	private boolean hashMatch;
	
}