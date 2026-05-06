package it.prismaprogetti.aimusei.service;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leonardo.aiservice.AiService;
import com.leonardo.aiservice.content.ByteArrayImage;
import com.leonardo.aiservice.content.JsonText;
import com.leonardo.aiservice.content.StandardText;
import com.leonardo.aiservice.content.TextContent;
import com.leonardo.aiservice.request.EtrRequest;
import com.leonardo.aiservice.request.TextGenerationRequest;
import com.leonardo.aiservice.response.MultilingualTextResponse;
import com.leonardo.aiservice.response.TextResponse;

import it.prismaprogetti.aimusei.collection.Sintesi;
import it.prismaprogetti.aimusei.model.TextGeneratedRequest;

@Service
public class SintesiService {
	
	@Autowired
	private AiService aiService;

	private boolean aiServiceActive=true;

	public Sintesi createSintesi(TextGeneratedRequest request) {
		
		return createSintesi(request.getOriginalText(), request.getContext());
	}

	public Sintesi createSintesi(String prompt, TextGeneratedRequest.Context context) {
		Sintesi sintesi = null;
		if (aiServiceActive) {
			
			TextContent promptContent = null;
			switch (context) {
			case ETR: {
				promptContent = StandardText.builder().value(prompt).build();
				break;
			}
			case INFO_MUSEO: {
				promptContent = JsonText.builder().value(prompt).build();
			}
			}
			EtrRequest etrRequest = EtrRequest.builder()
					.content(promptContent)
					.build();
			String descrizioneAi = null;
			descrizioneAi = ((TextResponse) aiService.sendRequest(etrRequest)).getContent().getValue();
			
			if(StringUtils.isBlank(descrizioneAi)) {
				throw new RuntimeException("AI service returned empty description");
			}

			return Sintesi.builder()
					.descrizioneAI(descrizioneAi)
					.validata(false)
					.generator("gpt-4")
					.dataInsert(LocalDateTime.now())
					.build();
		} else {
			//TODO ???
		}
		return sintesi;
	}
	
	
	public void activeAiService(Boolean activate) {
		this.aiServiceActive = activate;
	}

	public Boolean isActiveAiService() {
		return this.aiServiceActive;
	}

	public MultilingualTextResponse generateTextFromImageBytes(byte[] imageBytes, String hint) {
		ByteArrayImage byteArrayImage= ByteArrayImage.builder().value(imageBytes).build();
		MultilingualTextResponse response = (MultilingualTextResponse) aiService.sendRequest(TextGenerationRequest.builder().content(byteArrayImage).hint(hint).build());
		
		return response;
	}
}