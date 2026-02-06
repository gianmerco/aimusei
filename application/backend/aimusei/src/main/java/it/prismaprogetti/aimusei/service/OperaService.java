package it.prismaprogetti.aimusei.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperaService {

//	@Autowired
//	private OperaRepository repository;
//
//	public Opera getOpera(String tag) {
//		log.info("Start getOpera with tag: {}", tag);
//		Opera op = repository.findByTag(tag).orElse(null);
//		if (op == null) {
//			log.warn("Opera not found for tag: {}", tag);
//		} else {
//			log.debug("Opera found: {}", op);
//		}
//		log.info("End getOpera with tag: {}", tag);
//		return op;
//	}
//
//	public void modificaSintesi(ModificaSintesiRequest request) {
//		log.info("Start modificaSintesi with request: {}", request);
//		Optional<Opera> optional = repository.findByTag(request.getTag());
//		if (optional.isEmpty()) {
//			log.error("Opera not found for tag: {}", request.getTag());
//			return;
//		}
//		Opera opera = optional.get();
//		List<Sintesi> sintesi = opera.getSintesi();
//		
//		for (Sintesi sintesi2 : sintesi) {
//			if (sintesi2.getDisabilita().equals(request.getDisabilita().toString())) {
//				log.debug("Modifying Sintesi: {} with new description: {}", sintesi2, request.getNuovaSintesi());
//				sintesi2.setDescrizioneReviewed(request.getNuovaSintesi());
//				break;
//			}
//		}
//		repository.save(opera);
//		log.info("End modificaSintesi for tag: {}", request.getTag());
//	}
//
//	public void modificaFlagSintesi(ModificaFlagSintesiRequest request) {
//		log.info("Start modificaFlagSintesi with request: {}", request);
//		Optional<Opera> optional = repository.findByTag(request.getTag());
//		if (optional.isEmpty()) {
//			log.error("Opera not found for tag: {}", request.getTag());
//			return;
//		}
//		Opera opera = optional.get();
//		List<Sintesi> sintesi = opera.getSintesi();
//		for (Sintesi sintesi2 : sintesi) {
//			if (sintesi2.getDisabilita().equals(request.getDisabilita().toString())) {
//				log.debug("Modifying flag for Sintesi: {} to validata: {}", sintesi2, request.isValidata());
//				sintesi2.setValidata(request.isValidata());
//			}
//		}
//		repository.save(opera);
//		log.info("End modificaFlagSintesi for tag: {}", request.getTag());
//	}

}