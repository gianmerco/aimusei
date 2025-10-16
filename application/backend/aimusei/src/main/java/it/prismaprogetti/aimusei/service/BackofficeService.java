package it.prismaprogetti.aimusei.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.Sintesi;
import it.prismaprogetti.aimusei.exception.InvalidOpenAIKeyException;
import it.prismaprogetti.aimusei.model.Disabilita;
import it.prismaprogetti.aimusei.model.ElaboraTestiDisabilitaRequest;
import it.prismaprogetti.aimusei.model.TipoDisabilita;
import it.prismaprogetti.aimusei.repository.OperaRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BackofficeService {

	@Autowired
	private OpenAiService openAiService;

	@Autowired
	private OperaRepository repository;

	public Map<Disabilita, String> elaboraTestiDisabilita(ElaboraTestiDisabilitaRequest request)
			throws InvalidOpenAIKeyException, BadRequestException {
		log.info("Start elaboraTestiDisabilita for titolo: {}", request.getTitolo());
		log.debug("Request received: {}", request);
		checkTesto(request.getDescrizione());
		Map<Disabilita, String> testiElaboratiPerDisabilita = new HashMap<>();

		Opera opera = new Opera();
		opera.setNome(request.getTitolo());
		opera.setDescrizione(request.getDescrizione());
		opera.setTag(request.getTag());

		Sintesi sintesiDislessia = new Sintesi();
		sintesiDislessia.setDisabilita(TipoDisabilita.DISLESSIA);
		sintesiDislessia.setDescrizioneAI(openAiService.getDislessiaMock());
		sintesiDislessia.setValidata(false);

		Sintesi sintesiDiscalculia = new Sintesi();
		sintesiDiscalculia.setDisabilita(TipoDisabilita.DISCALCULIA);
		sintesiDiscalculia.setDescrizioneAI(openAiService.getDiscalculiaMock());
		sintesiDiscalculia.setValidata(false);

		opera.setSintesi(List.of(sintesiDiscalculia, sintesiDislessia));

		log.info("Saving Opera: {}", opera);
		repository.save(opera);

		String dislessiaText = openAiService.getDislessiaMock();
		String discalculiaText = openAiService.getDiscalculiaMock();
		log.debug("Generated text for DISLESSIA: {}", dislessiaText);
		log.debug("Generated text for DISCALCULIA: {}", discalculiaText);
		testiElaboratiPerDisabilita.put(Disabilita.DISLESSIA, dislessiaText);
		testiElaboratiPerDisabilita.put(Disabilita.DISCALCULIA, discalculiaText);

//		for (Disabilita disabilita : request.getDisabilita()) {
//
//			testiElaboratiPerDisabilita.put(disabilita, openAiService.prompt(testo+disabilita.getPrompt()));
//		}
		log.info("End elaboraTestiDisabilita for titolo: {}", request.getTitolo());
		return testiElaboratiPerDisabilita;
	}

	private void checkTesto(String testo) throws BadRequestException {
		if (!StringUtils.hasText(testo)) {
			log.error("Testo vuoto ricevuto in checkTesto");
			throw new BadRequestException("Testo vuoto");
		}
	}
}