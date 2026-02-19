package it.prismaprogetti.aimusei.model;

import com.leonardo.aiservice.response.MultilingualTextResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageToTextResponse {
	private MultilingualTextResponse multilingualTextResponse;
}