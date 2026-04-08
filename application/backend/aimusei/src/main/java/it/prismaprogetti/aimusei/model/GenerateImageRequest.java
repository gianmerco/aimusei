package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class GenerateImageRequest {

	private String json;
	private String idMuseo;
	private String imageUrl;
	
}
