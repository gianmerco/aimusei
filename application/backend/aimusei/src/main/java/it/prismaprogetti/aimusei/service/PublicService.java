package it.prismaprogetti.aimusei.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.Sintesi;
import it.prismaprogetti.aimusei.model.MassiveTextsResponse;
import it.prismaprogetti.aimusei.model.MetaMassiveTexts;
import it.prismaprogetti.aimusei.model.TagHashRequest;
import it.prismaprogetti.aimusei.model.TextHashedResponse;
import it.prismaprogetti.aimusei.model.TextHashedResponse.Status;
import it.prismaprogetti.aimusei.repository.OperaRepository;


@Service
public class PublicService {
	
	@Autowired
	private OperaRepository operaRepository;

//	public Map<String, Object> getLatestTextVersion(String tag, Object object) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public MassiveTextsResponse getMassiveTexts(List<TagHashRequest> list, String acceptLanguage) {
		int processed = 0;
		int ok = 0;
		int failed = 0;

		List<TextHashedResponse> results = new ArrayList<TextHashedResponse>();
		for (TagHashRequest tagHashRequest : list) {
			
			Opera opera = null;
			Status status = null;
			try {
				
				
				Optional<Opera> operaByTagOpt = operaRepository.findByTag(tagHashRequest.getTag());
				if (operaByTagOpt.isPresent()) {
					opera = operaByTagOpt.get();
					
					if (tagHashRequest.getHash().equals(opera.getHash())) {
						status = Status.OK;
					} else {
						//casistica di caso richiesta hash di versione precedente
						Optional<Opera> operaByHashOpt= operaRepository.findByHash(tagHashRequest.getHash());
						if(operaByHashOpt.isPresent()&&operaByHashOpt.get().getTag().equals(tagHashRequest.getTag())) {
							status = Status.OK;
							opera=operaByHashOpt.get();
						}
						else {
							status = Status.MISMATCH;
						}
					ok++;
				}
			} else {
					status = Status.NOT_FOUND;
					failed++;
				}
			} catch (Exception e) {
				status = Status.ERROR;
				failed++;
			} finally {
				processed++;
				
			results.add(	
				TextHashedResponse.builder()
				.tag(tagHashRequest.getTag())
				.hash(opera!=null?opera.getHash():null)
				.validator(opera!=null?opera.getValidator():null)
				.status(status)
				.version(opera!=null?opera.getVersion():null)
				.textGeneratedAI(opera!=null?getTextByTipoSemplificato(opera, false):null)
				.textRevisioned(opera!=null?getTextByTipoSemplificato(opera, true):null)
				.build()
				);
			}
		}
		MetaMassiveTexts meta = MetaMassiveTexts.builder()
				.processed(processed)
				.ok(ok)
				.failed(failed)
				.build();
		
		return new MassiveTextsResponse(results,meta);
	}
	
	private String getTextByTipoSemplificato(Opera opera,boolean revisioned) {
		Sintesi sintesi = opera.getSintesi();
		return revisioned ? sintesi.getDescrizioneReviewed() : sintesi.getDescrizioneAI();
		
	}

}