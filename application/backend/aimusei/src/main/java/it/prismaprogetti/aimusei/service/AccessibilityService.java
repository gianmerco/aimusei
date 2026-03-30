package it.prismaprogetti.aimusei.service;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.leonardo.aiservice.AiService;
import com.leonardo.aiservice.content.EtrText;
import com.leonardo.aiservice.content.StandardText;
import com.leonardo.aiservice.request.EtrRequest;
import com.leonardo.aiservice.request.PictogramsRequest;
import com.leonardo.aiservice.response.ImageResponse;
import com.leonardo.aiservice.response.MultilingualTextResponse;
import com.leonardo.aiservice.response.TextResponse;

import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.Sintesi;
import it.prismaprogetti.aimusei.controller.TextStatusResponse;
import it.prismaprogetti.aimusei.model.GenerateImageRequest;
import it.prismaprogetti.aimusei.model.HashValidateRequest;
import it.prismaprogetti.aimusei.model.ImageToTextRequest;
import it.prismaprogetti.aimusei.model.ImageToTextResponse;
import it.prismaprogetti.aimusei.model.RegenerateSintesiRequest;
import it.prismaprogetti.aimusei.model.StatoOpera;
import it.prismaprogetti.aimusei.model.TextGeneratedRequest;
import it.prismaprogetti.aimusei.model.TextGeneratedResponse;
import it.prismaprogetti.aimusei.model.TextHashValidateRequest;
import it.prismaprogetti.aimusei.model.TextOriginalResponse;
import it.prismaprogetti.aimusei.repository.OperaRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccessibilityService {

	@Autowired
	private OperaRepository operaRepository;
	
	@Autowired
	private SintesiService sintesiService;
	
	@Autowired
	private AiService aiService;
	
	@Autowired
	private PDFService pdfService;
	
	@Autowired
	private S3Service s3Service;

	@SneakyThrows
	@Transactional
/**
 * Gestione dei seguenti contesti:ETR,INFO_MUSEO;
 * 
 * ETR: generazione testo ETR, gestione dello stato nel nostro db del testo semplificato
 * 
 * @param request
 * @return
 */
	public TextGeneratedResponse generateSimplifiedTexts(TextGeneratedRequest request) {
		request.setContext(TextGeneratedRequest.Context.ETR);
		String hash = DigestUtils.md5DigestAsHex((request.getTag() + "#" + request.getOriginalText()).getBytes());

		Optional<Opera> operaByTagOPTLatest = operaRepository.findByTag(request.getTag());

		// Creazione ex novo
		if (operaByTagOPTLatest.isEmpty()) {
			Opera operaToInsert = Opera.builder().nome(request.getTitle()).descrizione(request.getOriginalText())
					.validator("gpt-4").version(0).hash(hash).tag(request.getTag()).lastUpdate(LocalDate.now())
					.latest(true).engineLLM("gpt-4").build();

			try {
				Sintesi sintesi = sintesiService.createSintesi(request);
				operaToInsert.setStatoOpera(StatoOpera.GENERATO_AI);
				operaToInsert.setSintesi(sintesi);
			} catch (Exception e) {
				operaToInsert.setStatoOpera(StatoOpera.INCOMPLETO);
			}

			operaRepository.save(operaToInsert);
			return TextGeneratedResponse.fromOpera(operaToInsert);
		}

		// Aggiornamento versione
		if (!hash.equals(operaByTagOPTLatest.get().getHash())) {
			
			// controllo se hash già esistente, in caso alla vecchia versione cancello
			// l'hash per evitare collisioni, questa casistica avviene quando si modifica il
			// testo originale con quello di una versione precedente
			operaRepository.findByHash(hash).ifPresent(o -> {
				o.setHash(null);
				operaRepository.save(o);
			});

			Opera operaByTag = operaByTagOPTLatest.get();

			operaByTag.setLatest(false);
			operaByTag.setLastUpdate(LocalDate.now());

			Opera newOperaVersion = Opera.builder().nome(operaByTag.getNome()).descrizione(request.getOriginalText())
					.version(operaByTag.getVersion() + 1).hash(hash).tag(request.getTag()).lastUpdate(LocalDate.now())
					.latest(true).engineLLM("gpt-4").build();

			try {
				Sintesi sintesi = sintesiService.createSintesi(request);
				newOperaVersion.setStatoOpera(StatoOpera.GENERATO_AI);
				newOperaVersion.setSintesi(sintesi);
			} catch (Exception e) {
				newOperaVersion.setStatoOpera(StatoOpera.INCOMPLETO);
			}

			operaRepository.save(operaByTag);
			operaRepository.save(newOperaVersion);

			return TextGeneratedResponse.fromOpera(newOperaVersion);
		}

		// hash uguale, ritorno quello esistente, prima effettuo un retry di
		// generazione sintesi nel caso non fossero tutte presenti
		Opera opera = operaByTagOPTLatest.get();

		try {
			Sintesi sintesi = sintesiService.createSintesi(request);
			opera.setStatoOpera(StatoOpera.GENERATO_AI);
			opera.setSintesi(sintesi);
		} catch (Exception e) {
			opera.setStatoOpera(StatoOpera.INCOMPLETO);
		}
		
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
	
	public TextStatusResponse getTextStatus(String tag, String originalText) {
		String hash = DigestUtils.md5DigestAsHex((tag + "#" + originalText).getBytes());

		Optional<Opera> operaByTagOPT = operaRepository.findByTag(tag);
		if (operaByTagOPT.isEmpty()) {
			return null;
		}
		boolean hashMatch = hash.equals(operaByTagOPT.get().getHash());
		
		Optional<Opera> operaByHash = operaRepository.findByHash(hash);
		if (operaByHash.isEmpty()) {
			return null;
		}
		
		return TextStatusResponse.builder()
				.hashMatch(hashMatch)
				.textStatus(operaByHash.get().getStatoOpera())
				.build();
	}


	public void reviseText(TextHashValidateRequest request) {
		updateSintesi(request.getHash(),  request.getText());
	}

	public void validateText(HashValidateRequest request) {
		updateSintesi(request.getHash(),  null);
	}


	private void updateSintesi(String hash, String descrizioneReviewed) {
		operaRepository.findByHash(hash).ifPresent(opera -> {
			Sintesi sintesi = opera.getSintesi();
			sintesi.setValidata(true);
			if (descrizioneReviewed != null) {
				sintesi.setDescrizioneReviewed(descrizioneReviewed);
			}
			opera.setStatoOpera(StatoOpera.REVISIONATO);
			operaRepository.save(opera);
		});
	}

	@Transactional
	@SneakyThrows
	public String regenerateSintesi(RegenerateSintesiRequest request) {

		Opera opera = operaRepository.findByHash(request.getHash())
				.orElseThrow(() -> new Exception("Opera non trovata"));

		Sintesi sintesiToUpdate = opera.getSintesi();

		EtrRequest etrRequest = EtrRequest.builder().content(StandardText.builder().value(opera.getDescrizione()).build()).build();
		String descrizioneAi=null;
			descrizioneAi=((TextResponse)aiService.sendRequest(etrRequest)).getContent().getValue();
		

		sintesiToUpdate.setDescrizioneAI(descrizioneAi);
		sintesiToUpdate.setValidata(false);

		operaRepository.save(opera);
		return descrizioneAi;
	}

	@SneakyThrows
	public byte[] generateImage(GenerateImageRequest request) {
				
		String etr=sintesiService.createSintesi(request.getJson(),TextGeneratedRequest.Context.INFO_MUSEO).getDescrizioneAI();
		
		PictogramsRequest pictogramsRequest = PictogramsRequest.builder()
		.content(EtrText.builder()
				.value(etr)
				.build()
				)
		.museumImage(request.getImageContent())
		.build();
		
		ImageResponse imageResponse=((ImageResponse)aiService.sendRequest(pictogramsRequest));
		
		byte[] document = pdfService.generateDocument(imageResponse.getContent());

		s3Service.saveInBucket(document,request.getIdMuseo());	
		
		return document;
	}
	
	@SneakyThrows
	public byte[] generateImageTest(GenerateImageRequest request) {
				
		String etr=request.getJson();
		
		PictogramsRequest pictogramsRequest = PictogramsRequest.builder()
		.content(EtrText.builder()
				.value(etr)
				.build()
				)
		.museumImage(request.getImageContent())
		.build();
		
		ImageResponse imageResponse=((ImageResponse)aiService.sendRequest(pictogramsRequest));
		
		byte[] document = pdfService.generateDocument(imageResponse.getContent());

		s3Service.saveInBucket(document,request.getIdMuseo());	
		
		return document;
	}


	public URL getImageUrl(String idMuseo) {
		return s3Service.getSignedGetUrl(idMuseo);
	}

	public Boolean imageExists(String idMuseo) {
		
        return s3Service.objectExists(idMuseo);
	}

	public byte[] getImage(String idMuseo) {
		return s3Service.getImage(idMuseo);
	}

	public ImageToTextResponse generateTextFromImage(ImageToTextRequest request) {
		boolean hasBase64 = request.getBase64() != null && !request.getBase64().isEmpty();
		boolean hasUrl = request.getUrl() != null && !request.getUrl().isEmpty();

		if (hasBase64 && hasUrl) {
			throw new IllegalArgumentException("Both base64 and url are provided. Only one is allowed.");
		}
		if (!hasBase64 && !hasUrl) {
			throw new IllegalArgumentException("Either base64 or url must be provided.");
		}
		 // 2. Ottenimento dei byte dell'immagine
        byte[] imageBytes;
        if (hasBase64) {
            // Decodifica del Base64
        	try {

        	    String base64String = request.getBase64().replaceAll("\\s", ""); // Rimuovere gli spazi
        	    // Rimuovere eventuale prefisso
        	    if (base64String.startsWith("data:image/png;base64,"))
        	        base64String = base64String.substring("data:image/png;base64,".length());
        	    else if (base64String.startsWith("data:image/jpeg;base64,"))
        	        base64String = base64String.substring("data:image/jpeg;base64,".length());
        	    imageBytes = Base64.getDecoder().decode(base64String);
        	} catch (IllegalArgumentException e) {
        		throw new IllegalArgumentException("Invalid base64 string", e);
        	}
        } else {
            // Download dell'immagine dall'URL
            imageBytes = downloadImageFromUrl(request.getUrl());
        }
        // 3. Generazione del testo dall'immagine (da implementare)
        log.info("Generating text from image with hint: {}", request.getHint());
        MultilingualTextResponse generatedText = sintesiService.generateTextFromImageBytes(imageBytes,request.getHint());

        return new ImageToTextResponse(generatedText);
	}
	
	private byte[] downloadImageFromUrl(String urlString) {
	    try {
	        TrustManager trustAll = new X509TrustManager() {
	            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	            @Override public void checkClientTrusted(X509Certificate[] c, String a) { }
	            @Override public void checkServerTrusted(X509Certificate[] c, String a) { }
	        };
	        
	        
	        log.info("Downloading image from URL: {}", urlString);
	        
	        SSLContext sslCtx = SSLContext.getInstance("TLS");
	        sslCtx.init(null, new TrustManager[]{ trustAll }, null);

	        URL url = URI.create(urlString).toURL();
	        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	        conn.setSSLSocketFactory(sslCtx.getSocketFactory());
	        conn.setHostnameVerifier((h, s) -> true);
	        try (InputStream in = conn.getInputStream()) {
	            return in.readAllBytes();
	        }
	    } catch (IOException e) {
	        throw new RuntimeException("Failed to download image from URL: " + urlString, e);
	    } catch (Exception e) {
	        throw new RuntimeException("SSL setup failed for URL: " + urlString, e);
	    }
	}
}