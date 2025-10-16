package it.prismaprogetti.aimusei.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.prismaprogetti.aimusei.model.Disabilita;
import it.prismaprogetti.aimusei.repository.OperaRealTimeRepository;
import it.prismaprogetti.aimusei.repository.OperaRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

	@Autowired
	private OperaRepository repository;

	@Autowired
	private OperaRealTimeRepository repositoryRealTime;

	public String getDescrizioneOpera(String nomeOpera) {
		log.info("Start getDescrizioneOpera for opera: {}", nomeOpera);
		String descrizione = repository.findDescrizioneByNome(nomeOpera).orElse(null);
		if (descrizione == null) {
			log.warn("Descrizione not found for opera: {}", nomeOpera);
		} else {
			log.debug("Descrizione found: {}", descrizione);
		}
		log.info("End getDescrizioneOpera for opera: {}", nomeOpera);
		return descrizione;
	}

	public String getSintesi(String nomeOpera, Disabilita disabilita) {
		log.info("Start getSintesi for opera: {}, disabilita: {}", nomeOpera, disabilita);
		String sintesi = repository.findSintesiByNome(nomeOpera, disabilita.toString()).orElse(null);
		if (sintesi == null) {
			log.warn("Sintesi not found for opera: {}, disabilita: {}", nomeOpera, disabilita);
		} else {
			log.debug("Sintesi found: {}", sintesi);
		}
		log.info("End getSintesi for opera: {}, disabilita: {}", nomeOpera, disabilita);
		return sintesi;
	}

	public String elabora(String nomeOpera, String disabilita) {
		log.info("Start elabora for opera: {}, disabilita: {}", nomeOpera, disabilita);
		if (!disabilita.equals("null")) {
			String sintesi = repositoryRealTime.findSintesiByNome(nomeOpera, disabilita.toUpperCase()).orElse(null);
			if (sintesi == null) {
				log.warn("Sintesi not found in real time for opera: {}, disabilita: {}", nomeOpera, disabilita);
			} else {
				log.debug("Real time sintesi found: {}", sintesi);
			}
			log.info("End elabora for opera: {}, disabilita: {}", nomeOpera, disabilita);
			return sintesi;
		} else {
			String descrizione = repositoryRealTime.findDescrizioneByNome(nomeOpera).orElse(null);
			if (descrizione == null) {
				log.warn("Descrizione not found in real time for opera: {}", nomeOpera);
			} else {
				log.debug("Real time descrizione found: {}", descrizione);
			}
			log.info("End elabora for opera: {}, disabilita: null", nomeOpera);
			return descrizione;
		}
	}

}