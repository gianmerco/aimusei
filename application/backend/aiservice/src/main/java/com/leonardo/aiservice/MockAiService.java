package com.leonardo.aiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leonardo.aiservice.content.StandardText;
import com.leonardo.aiservice.request.AiRequest;
import com.leonardo.aiservice.request.EtrRequest;
import com.leonardo.aiservice.request.PictogramsRequest;
import com.leonardo.aiservice.response.AiResponse;
import com.leonardo.aiservice.response.ImageResponse;
import com.leonardo.aiservice.response.TextResponse;

/**
 * implementazione standard di AiService. La libreria è aperta a ulteriori implementazioni in futuro
 */
public class MockAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(MockAiService.class);

	@Override
	public AiResponse sendRequest(AiRequest request) {
		log.info("MockAiService: ricevuta richiesta di tipo {}", request.getClass().getSimpleName());
		
		
		if(request instanceof EtrRequest) {
			TextResponse mockResponse = TextResponse.builder()
					.content(StandardText.builder().value(((EtrRequest) request).getContent().getValue() + "-MOCKED").build()).build();
			return mockResponse;
		}
		if(request instanceof PictogramsRequest) {
			ImageResponse mockResponse = ImageResponse.builder().build();
			return mockResponse;
		}
		
		throw new UnsupportedOperationException("MockAiService non supporta richieste di tipo " + request.getClass().getSimpleName());
	}
}
