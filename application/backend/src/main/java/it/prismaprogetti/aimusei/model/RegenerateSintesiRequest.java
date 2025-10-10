package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class RegenerateSintesiRequest {
	private TipoDisabilita sintesi;
	private String tag;
	private String hash;
}
