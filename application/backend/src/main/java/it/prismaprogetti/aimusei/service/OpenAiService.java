package it.prismaprogetti.aimusei.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.leonardo.aiservice.AIService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import it.prismaprogetti.aimusei.exception.InvalidOpenAIKeyException;

@Service
public class OpenAiService {

	@Value("${aimusei.key}")
	private String openAIApiKey;
	
	@Autowired
	private AIService aiService;

	private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

	private RestTemplate restTemplate = new RestTemplate();

	public String getDislessiaMock() {
		return "La Notte stellata è un famoso dipinto di Van Gogh."
				+ " Il pittore lo ha creato nel maggio del 1889, quando era in un ospedale psichiatrico "
				+ "a Saint-Rémy de Provence, in Francia. Prima di andare lì, aveva avuto problemi con un amico, Gauguin, "
				+ "e si era fatto del male all’orecchio sinistro. Questo episodio triste lo portò a decidere di farsi "
				+ "ricoverare.Anche se quel periodo fu molto difficile per lui, fu anche molto produttivo. Infatti, in un solo anno,"
				+ " Van Gogh dipinse più di 150 quadri, e uno di questi è La Notte stellata. Questo dipinto è uno dei più famosi "
				+ "e mostra un cielo notturno pieno di stelle.\r\n";
	}

	public String getDiscalculiaMock() {
		return "Vincent Van Gogh, un famoso pittore, dopo un periodo difficile nella sua vita personale e"
				+ " una lite con l'amico Gauguin, decide di ricoverarsi in una clinica psichiatrica a"
				+ " Saint-Rémy de Provence. Durante questo tempo, nonostante le difficoltà, "
				+ "Van Gogh trova l'ispirazione per dipingere moltissimo. Uno dei suoi quadri più famosi realizzati"
				+ " in questo periodo è \"La Notte Stellata\".";
	}

	public String prompt(String prompt) throws InvalidOpenAIKeyException {

		// MOCK
		return prompt;

//		HttpEntity<ChatCompletionRequest> requestEntity = new HttpEntity<ChatCompletionRequest>(
//				createChatCompletationRequest(prompt), createAuthorizationHeader());
//
//		ChatCompletionResult chatCompletionResult = null;
//		try {
//			chatCompletionResult = restTemplate
//					.exchange(OPENAI_API_URL, HttpMethod.POST, requestEntity, ChatCompletionResult.class).getBody();
//		} catch (Unauthorized e) {
//			throw new InvalidOpenAIKeyException("OpenAIKey non autorizzata");
//		}
//
//		return chatCompletionResult.getChoices().get(0).getMessage().getContent();
	}

	private ChatCompletionRequest createChatCompletationRequest(String prompt) {
		ChatCompletionRequest request = new ChatCompletionRequest();
		request.setModel("gpt-4o");
		request.setMessages(List.of(new ChatMessage("user", "prompt")));
		request.setN(1);
		return request;
	}

	private HttpHeaders createAuthorizationHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(openAIApiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

}