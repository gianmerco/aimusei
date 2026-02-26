package it.prismaprogetti.aimusei.model;

import java.util.Map;

import com.leonardo.aiservice.content.ImageContent;

import lombok.Data;

@Data
public class GenerateImageRequest {

	private String tag;
	private String idMuseo;
	private Map<String, ImageContent> wordsToPictograms;
	
}
