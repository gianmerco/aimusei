package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class HashValidateRequest {
	private TipoDisabilita sintesi;
	private String tag;
	private String hash;
}
