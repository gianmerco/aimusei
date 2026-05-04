package it.prismaprogetti.aimusei.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.leonardo.aiservice.AiService;
import com.leonardo.aiservice.content.BatchTextContent;
import com.leonardo.aiservice.content.StandardText;
import com.leonardo.aiservice.content.TextContent;
import com.leonardo.aiservice.request.EtrMassiveRequest;
import com.leonardo.aiservice.request.MassivePollingRequest;
import com.leonardo.aiservice.response.BatchStatus;
import com.leonardo.aiservice.response.BatchTextResponse;
import com.leonardo.aiservice.response.TextResponse;

import it.prismaprogetti.aimusei.collection.Job;
import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.OperaToInsert;
import it.prismaprogetti.aimusei.collection.OperaToInsert.Status;
import it.prismaprogetti.aimusei.collection.Sintesi;
import it.prismaprogetti.aimusei.model.StatoJob;
import it.prismaprogetti.aimusei.model.StatoOpera;
import it.prismaprogetti.aimusei.repository.JobRepository;
import it.prismaprogetti.aimusei.repository.OperaRepository;
import it.prismaprogetti.aimusei.repository.OperaToInsertRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MassiveService {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private BatchPreleaver batchPreleaver;

	@Autowired
	private OperaRepository operaRepository;
	
	@Autowired
	private OperaToInsertRepository operaToInsertRepository;

	@Autowired
	private AiService aiService;
	
	@Value("${polling.timeout-minutes:30}")
	private long pollingTimeoutMinutes;

	@Value("${polling.interval-seconds:90}")
	private long pollingIntervalSeconds;

	@Value("${polling.max-consecutive-errors:3}")
	private int maxConsecutiveErrors;
	
	@Value("${csv.batch-size:100}")
	private int csvBatchSize;

	public String massiveGenerate(MultipartFile file) {
		Job job = jobRepository.save(createJob());
		populateOperaFromCSV(file, job);

		return job.getId();
	}

	@Async
	private void populateOperaFromCSV(MultipartFile file, Job job) {

		Set<CsvEntry> batch = new HashSet<>(csvBatchSize);
		int opereParsed = 0;
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

			// skip prima riga
			reader.readLine();

			String line;
			while ((line = reader.readLine()) != null) {
				// parsing
				try {
					if (line.isBlank()) {
						continue;
					}
					int firstSep = line.indexOf(';');
					if (firstSep == -1) {
						continue;
					}

					String tag = line.substring(0, firstSep).trim();
					String descrizione = line.substring(firstSep + 1).trim();
					batch.add(new CsvEntry(tag, descrizione));
					opereParsed++;
				} catch (Exception e) {
					continue;
				}
				if (batch.size() == csvBatchSize) {
					insertOperaBatch(batch, job);
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				insertOperaBatch(batch, job);
				batch.clear();
			}
		} catch (Exception e) {
			job.setStato(StatoJob.ERRORE_IN_FASE_DI_CREAZIONE);
			jobRepository.save(job);
			
			sanitizeOpereToInsertByJobId(job.getId());
			
			throw new RuntimeException("Errore nella lettura del file CSV", e);
		}

		job.setOpereParsed(opereParsed);
		jobRepository.save(job);
	}

	private void sanitizeOpereToInsertByJobId(String id) {
		Query query = new Query(Criteria.where("jobId").is(id));
		 Update update = new Update()
		            .set("status", OperaToInsert.Status.ERROR);
		    mongoTemplate.updateMulti(query, update, OperaToInsert.class);
	}

	private void insertOperaBatch(Set<CsvEntry> batch, Job job) {
		try {
			Set<OperaToInsert> operaBatch = new HashSet<>(batch.size());

			operaBatch = batch.stream().map(entry -> OperaToInsert.builder().tag(entry.tag)
					.descrizione(entry.descrizione).jobId(job.getId()).status(Status.PENDING).build()).collect(Collectors.toSet());

			mongoTemplate.insert(operaBatch,OperaToInsert.class);
		} catch (Exception e) {
			log.error("Errore nell'inserimento batch di opere", e);
			throw e;
		}
	}

	private Job createJob() {
		return Job.builder().creationDateTime(LocalDateTime.now()).stato(StatoJob.PENDING).build();

	}

	public record CsvEntry(String tag, String descrizione) {
	}

	public void processaOpere() {

		// Preleva un batch di opere in stato PENDING e portale in PROCESSING
		List<OperaToInsert> batchAtomico = batchPreleaver.prelevaBatchUpdateProcessingAtomicoBulk();
		if(batchAtomico.isEmpty()) {
			return;
		}
		TextContent batchIdContent = null;
		Map<String, TextContent> entries = new HashMap<String, TextContent>();
		try {
			batchAtomico.forEach(opera -> {
				entries.put(opera.getTag(), StandardText.builder().value(opera.getDescrizione()).build());
			});
			EtrMassiveRequest request = EtrMassiveRequest.builder()
					.content(BatchTextContent.builder().entries(entries).build()).build();

			// Richiesta batch al servizio AI e ottieni batchId
			
			//TODO qui possibile bug AiGateway ritorna un ETRtext invece di un SimpleText
			String batchId= ((TextResponse) aiService.sendRequest(request)).getContent().getValue();
			batchIdContent=StandardText.builder().value(batchId).build();
		} catch (Exception e) {
			log.error("Fallita sottomissione batch", e);
			batchPreleaver.portaBatchInError(batchAtomico);
			return;
		}

		// polling
		BatchTextContent batchTextContent = polling(batchAtomico, batchIdContent);
		if (batchTextContent == null) {
			return; // il batch è già stato portato in error dal polling
		}
		// da qui possiamo assumere che i testi semplificati siano stati ottenuti,
		// ancora da verificare che qualcuno non sia in error
		
		try {
		Map<String, Opera> opere = creaOpere(batchTextContent, entries);
		
		updateOpereToInsertWithOpere(batchAtomico, opere);
		
		insertOpere(opere.values());
		insertOpereToInsert(batchAtomico);
		} catch (Exception e) {
			log.error("Fallita insert opere", e);
			batchPreleaver.portaBatchInError(batchAtomico);
			return;
		}

	}
	private void insertOpereToInsert(List<OperaToInsert> batchAtomico) {
	    if (batchAtomico == null || batchAtomico.isEmpty()) {
	        return;
	    }
	    
	    operaToInsertRepository.saveAll(batchAtomico);
	}

	private void insertOpere(Collection<Opera> opere) {
	    if (opere == null || opere.isEmpty()) {
	        return;
	    }
	    
	    operaRepository.saveAll(opere);
	}

	private void updateOpereToInsertWithOpere(List<OperaToInsert> batchAtomico, Map<String, Opera> opere) {
		
		for (OperaToInsert operaToInsert : batchAtomico) {
			Opera opera=opere.get(operaToInsert.getTag());
			boolean error=opera.getStatoOpera().equals(StatoOpera.INCOMPLETO);
			operaToInsert.setStatus(error? OperaToInsert.Status.ERROR: OperaToInsert.Status.GENERATED);
		}
	}

	private Map<String,Opera> creaOpere(BatchTextContent batchTextContent, Map<String, TextContent> entries) {

		Map<String,Opera> opere = new HashMap<>();

		batchTextContent.getEntries().forEach((tag, textContentGenerato) -> {
			String originalText = entries.get(tag).getValue();
			String hash = DigestUtils.md5DigestAsHex((tag + "#" + originalText).getBytes());

			boolean isError = textContentGenerato.getValue().contains("ERRORE");

			Optional<Opera> operaByTagOPTLatest = operaRepository.findByTag(tag);
			// Creazione ex novo
			if (operaByTagOPTLatest.isEmpty()) {
				Opera operaToInsert = Opera.builder().descrizione(originalText).validator("gpt-4").version(0).hash(hash)
						.tag(tag).lastUpdate(Instant.now()).latest(true).engineLLM("gpt-4")
						.statoOpera(isError ? StatoOpera.INCOMPLETO : StatoOpera.GENERATO_AI)
						.sintesi(isError ? null
								: Sintesi.builder().descrizioneAI(textContentGenerato.getValue()).validata(false)
										.generator("gpt-4").dataInsert(LocalDateTime.now()).build())
						.build();
				opere.put(tag,operaToInsert);
			} 
			// Aggiornamento versione
			else if (!hash.equals(operaByTagOPTLatest.get().getHash())) {
				
				// controllo se hash già esistente, in caso alla vecchia versione cancello
				// l'hash per evitare collisioni, questa casistica avviene quando si modifica il
				// testo originale con quello di una versione precedente
				operaRepository.findByHash(hash).ifPresent(o -> {
					o.setHash(null);
					operaRepository.save(o);
				});

				Opera operaByTag = operaByTagOPTLatest.get();

				operaByTag.setLatest(false);
				operaByTag.setLastUpdate(Instant.now());

				Opera newOperaVersion = Opera.builder().descrizione(originalText)
						.version(operaByTag.getVersion() + 1).hash(hash).tag(tag).lastUpdate(Instant.now())
						.latest(true).engineLLM("gpt-4")
						.statoOpera(isError ? StatoOpera.INCOMPLETO : StatoOpera.GENERATO_AI)
						.sintesi(isError ? null
								: Sintesi.builder().descrizioneAI(textContentGenerato.getValue()).validata(false)
										.generator("gpt-4").dataInsert(LocalDateTime.now()).build())
						.build();
				opere.put(tag,newOperaVersion);
			} else {
				// hash uguale, ritorno quello esistente, prima effettuo un retry di
				// generazione sintesi nel caso non fossero tutte presenti
				Opera opera = operaByTagOPTLatest.get();
				
				opera.setStatoOpera(isError ? StatoOpera.INCOMPLETO : StatoOpera.GENERATO_AI);
				opera.setSintesi(isError ? null
								: Sintesi.builder().descrizioneAI(textContentGenerato.getValue()).validata(false)
										.generator("gpt-4").dataInsert(LocalDateTime.now()).build());
				opere.put(tag,opera);
			}
		});
		return opere;
	}

	private BatchTextContent polling(List<OperaToInsert> batchAtomico, TextContent batchIdContent) {
		String batchId = batchIdContent.getValue();

		MassivePollingRequest pollingRequest = MassivePollingRequest.builder().content(batchIdContent).build();

		Instant deadline = Instant.now().plus(pollingTimeoutMinutes, ChronoUnit.MINUTES);

		BatchStatus status = null;
		BatchTextContent simplifiedTexts = null;
		
		  int consecutiveErrors = 0; // contatore errori consecutivi

		try {
			while (Instant.now().isBefore(deadline)) {
				try {
					BatchTextResponse response = (BatchTextResponse) aiService.sendRequest(pollingRequest);
					status = response.getStatus();
					
	                // Richiesta andata a buon fine → azzera il contatore di errori
	                consecutiveErrors = 0;

					if (status.equals(BatchStatus.COMPLETED)) {
						simplifiedTexts = response.getContent();
						break;
					} else if (status.equals(BatchStatus.FAILED)) {
						break;
					}
					// non ancora terminato: attendi prima del prossimo tentativo,
					// ma senza superare la scadenza
					long remainingMs = Duration.between(Instant.now(), deadline).toMillis();
					if (remainingMs <= 0)
						break;

					long sleepMs = Math.min(TimeUnit.SECONDS.toMillis(pollingIntervalSeconds), remainingMs);
					Thread.sleep(sleepMs);

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("Polling interrotto per il batch {}", batchId);
					break; // il thread termina, le opere to insert vanno in ERROR

				} catch (Exception e) {
					log.error("Errore durante polling del batch {}", batchId, e);
					consecutiveErrors++;
					
					 if (consecutiveErrors >= 3) {
		                    log.error("Troppi errori consecutivi per il batch {}. Interruzione del polling.", batchId);
		                    break; // esce dal while e va al finally
		                }
					
					// errore transiente: attendi e riprova, sempre rispettando il timeout
					long remainingMs = Duration.between(Instant.now(), deadline).toMillis();
					if (remainingMs <= 0)
						break;
					long sleepMs = Math.min(TimeUnit.SECONDS.toMillis(pollingIntervalSeconds), remainingMs);
					Thread.sleep(sleepMs);
				}
			}
		} catch (Exception e) {
			log.error("Errore imprevisto nel polling", e);
		} finally {
			if (simplifiedTexts == null) {
				batchPreleaver.portaBatchInError(batchAtomico);
				return null;
			}

		}
		return simplifiedTexts;
	}
}