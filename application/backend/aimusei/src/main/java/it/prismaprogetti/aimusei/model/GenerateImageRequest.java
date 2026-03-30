package it.prismaprogetti.aimusei.model;

import java.util.Map;

import com.leonardo.aiservice.content.ImageContent;

import lombok.Data;

@Data
public class GenerateImageRequest {

	private String json;
	private String idMuseo;
	private ImageContent imageContent;
	
}
