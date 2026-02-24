package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class ImageToTextRequest {
	private String base64; // Immagine codificata in Base64
	private String url; // URL pubblico o firmato dell'immagine su bucket
	private String hint; 
}