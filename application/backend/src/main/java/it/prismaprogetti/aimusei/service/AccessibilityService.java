package it.prismaprogetti.aimusei.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.Sintesi;
import it.prismaprogetti.aimusei.model.HashValidateRequest;
import it.prismaprogetti.aimusei.model.RegenerateSintesiRequest;
import it.prismaprogetti.aimusei.model.StatoOpera;
import it.prismaprogetti.aimusei.model.StatusResponse;
import it.prismaprogetti.aimusei.model.TextGeneratedRequest;
import it.prismaprogetti.aimusei.model.TextGeneratedResponse;
import it.prismaprogetti.aimusei.model.TextHashValidateRequest;
import it.prismaprogetti.aimusei.model.TextOriginalResponse;
import it.prismaprogetti.aimusei.model.TipoDisabilita;
import it.prismaprogetti.aimusei.repository.OperaRepository;
import lombok.SneakyThrows;

@Service
public class AccessibilityService {

	@Autowired
	private OperaRepository operaRepository;

	@Autowired
	private OpenAiService openAiService;

	@SneakyThrows
	@Transactional
	public TextGeneratedResponse generateSimplifiedTexts(TextGeneratedRequest request) {
		String hash = DigestUtils.md5DigestAsHex((request.getTag() + "#" + request.getOriginalText()).getBytes());

		Optional<Opera> operaByTagOPT = operaRepository.findByTag(request.getTag());

		if (operaByTagOPT.isEmpty()) {
			Opera operaToInsert = Opera.builder().nome(request.getTitle()).descrizione(request.getOriginalText())
					.validator("gpt-4").version(0).hash(hash).tag(request.getTag()).lastUpdate(LocalDate.now())
					.latest(true).statoOpera(StatoOpera.INCOMPLETO).engineLLM("gpt-4").build();

			List<Sintesi> sintesis = null;
			try {
				sintesis = createSintesi(request.getOriginalText());
				operaToInsert.setStatoOpera(StatoOpera.GENERATO_AI);
			} catch (SintesiWrapperException e) {
				sintesis = e.getSintesiParziali();
			} catch (Exception e) {
				throw e;
			}
			operaToInsert.setSintesi(sintesis);

			operaRepository.save(operaToInsert);
			return TextGeneratedResponse.fromOpera(operaToInsert);
		}

		if (!hash.equals(operaByTagOPT.get().getHash())) {
			Opera operaByTag = operaByTagOPT.get();

			operaByTag.setLatest(false);
			operaByTag.setLastUpdate(LocalDate.now());

			Opera newOperaVersion = Opera.builder().nome(operaByTag.getNome()).descrizione(request.getOriginalText())
					.version(operaByTag.getVersion() + 1).hash(hash).tag(request.getTag()).lastUpdate(LocalDate.now())
					.latest(true).statoOpera(StatoOpera.INCOMPLETO).engineLLM("gpt-4").build();

			List<Sintesi> sintesis = null;
			try {
				sintesis = createSintesi(request.getOriginalText());
				newOperaVersion.setStatoOpera(StatoOpera.GENERATO_AI);
			} catch (SintesiWrapperException e) {
				sintesis = e.getSintesiParziali();
			} catch (Exception e) {
				throw e;
			}
			newOperaVersion.setSintesi(sintesis);

			operaRepository.save(operaByTag);
			operaRepository.save(newOperaVersion);

			return TextGeneratedResponse.fromOpera(newOperaVersion);
		}

		// hash uguale, ritorno quello esistente, TODO prima effettuo un retry di
		// generazione sintesi nel caso non fossero tutte presenti
		Opera opera = operaByTagOPT.get();
		List<Sintesi> sintesis = null;

		try {
			List<TipoDisabilita> disbilitaToExlude = opera.getSintesi().stream().map(s -> s.getDisabilita()).toList();
			sintesis = createSintesi(opera.getDescrizione(), disbilitaToExlude);
			opera.setStatoOpera(StatoOpera.GENERATO_AI);
		} catch (SintesiWrapperException e) {
			sintesis = e.getSintesiParziali();
		} catch (Exception e) {
			throw e;
		}
		opera.getSintesi().addAll(sintesis);
		operaRepository.save(opera);

		return TextGeneratedResponse.fromOpera(opera);
	}

	public TextOriginalResponse getOriginalText(String tag, String originalText) {
		String hash = DigestUtils.md5DigestAsHex((tag + "#" + originalText).getBytes());

		Optional<Opera> operaByTagOPT = operaRepository.findByTag(tag);
		if (operaByTagOPT.isEmpty()) {
			return null;
		}
		boolean hashMatch = hash.equals(operaByTagOPT.get().getHash());

		return TextOriginalResponse.fromOpera(operaByTagOPT.get(), hashMatch);
	}

	public StatusResponse getTextStatus(String tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public void reviseText(TextHashValidateRequest request) {
		updateSintesi(request.getHash(), request.getSintesi(), request.getText());
	}

	public void validateText(HashValidateRequest request) {
		updateSintesi(request.getHash(), request.getSintesi(), null);
	}

	private List<Sintesi> createSintesi(String prompt) throws SintesiWrapperException {
		return createSintesi(prompt, new ArrayList<>());
	}

	private List<Sintesi> createSintesi(String prompt, List<TipoDisabilita> toExclude) throws SintesiWrapperException {
		List<Sintesi> sintesiList = new ArrayList<>();
		List<TipoDisabilita> tipiDisabilita = new ArrayList<>(Arrays.asList(TipoDisabilita.values()));

		tipiDisabilita.removeAll(toExclude);

		for (TipoDisabilita tipo : tipiDisabilita) {
			try {
				String descrizioneAI = openAiService.prompt(prompt + " " + tipo.name().toLowerCase() + ".");
				Sintesi sintesi = Sintesi.builder().disabilita(tipo).descrizioneAI(descrizioneAI).validata(false)
						.generator("gpt-4").dataInsert(LocalDateTime.now()).build();
				sintesiList.add(sintesi);
			} catch (Exception e) {
				// Creiamo un'eccezione che contiene le sintesi generate fino a ora
				throw new SintesiWrapperException(
						"Errore durante la generazione della sintesi per " + tipo + ". Sintesi generate: "
								+ sintesiList.size(),
						new ArrayList<>(sintesiList), // Copia della lista corrente
						e);
			}
		}

		return sintesiList;
	}

	private void updateSintesi(String hash, TipoDisabilita tipoDisabilita, String descrizioneReviewed) {
		operaRepository.findByHash(hash).ifPresent(opera -> {
			opera.getSintesi().stream()
					.filter(sintesi -> sintesi.getDisabilita().equals(tipoDisabilita) && !sintesi.isValidata())
					.findFirst().ifPresent(sintesi -> {
						sintesi.setValidata(true);
						if (descrizioneReviewed != null) {
							sintesi.setDescrizioneReviewed(descrizioneReviewed);
						}
						operaRepository.save(opera);
					});
		});
	}

	public class SintesiWrapperException extends Exception {
		private final List<Sintesi> sintesiParziali;

		public SintesiWrapperException(String message, List<Sintesi> sintesiParziali, Throwable cause) {
			super(message, cause);
			this.sintesiParziali = sintesiParziali;
		}

		public List<Sintesi> getSintesiParziali() {
			return sintesiParziali;
		}
	}

	@Transactional
	@SneakyThrows
	public String regenerateSintesi(RegenerateSintesiRequest request) {

		  Opera opera = operaRepository.findByHash(request.getHash())
		            .orElseThrow(() -> new RuntimeException("Opera non trovata"));
		  
		  Sintesi sintesiToUpdate = opera.getSintesi().stream()
		            .filter(sintesi -> sintesi.getDisabilita().equals(request.getSintesi()) && !sintesi.isValidata())
		            .findFirst()
		            .orElseThrow(() -> new RuntimeException("Sintesi non trovata"));
		  
		  
		  String newAiText= openAiService.prompt(opera.getDescrizione() + " " + request.getSintesi().name().toLowerCase() + ".");
		  
		  sintesiToUpdate.setDescrizioneAI(newAiText);
		  sintesiToUpdate.setValidata(false);
		  
		  operaRepository.save(opera);
		  return newAiText;
	}

}