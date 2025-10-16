package it.prismaprogetti.aimusei.controller;

import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.prismaprogetti.aimusei.exception.InvalidOpenAIKeyException;
import it.prismaprogetti.aimusei.model.Disabilita;
import it.prismaprogetti.aimusei.model.ElaboraTestiDisabilitaRequest;
import it.prismaprogetti.aimusei.model.ModificaFlagSintesiRequest;
import it.prismaprogetti.aimusei.model.ModificaSintesiRequest;
import it.prismaprogetti.aimusei.service.BackofficeService;
import it.prismaprogetti.aimusei.service.OperaService;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@CrossOrigin(origins = "*")
public class BackofficeController {

	@Autowired
	private BackofficeService aiMuseiService;

	@Autowired
	private OperaService operaService;

	@CrossOrigin(origins = "*") 
	@PostMapping("/elaboraTestiDisabilita")
	public ResponseEntity<Map<Disabilita, String>> elaboraTestiDisabilita(HttpServletRequest request,
			@RequestBody ElaboraTestiDisabilitaRequest requestElaborazione)
			throws BadRequestException, InvalidOpenAIKeyException {
		return ResponseEntity.ok(aiMuseiService.elaboraTestiDisabilita(requestElaborazione));
	}

	@CrossOrigin(origins = "*") 
	@GetMapping("/getOpera")
	public ResponseEntity<?> getDescrizioneOpera(@RequestParam String tag) {
		return ResponseEntity.ok(operaService.getOpera(tag));
	}

	@CrossOrigin(origins = "*") 
	@PostMapping("/modificaSintesi")
	public ResponseEntity<?> modificaSintesi(@RequestBody ModificaSintesiRequest request) {
		operaService.modificaSintesi(request);

		return ResponseEntity.ok().build();
	}
	
	@CrossOrigin(origins = "*") 
	@PostMapping("/modificaFlagSintesi")
	public ResponseEntity<?> modificaFlagSintesi(@RequestBody ModificaFlagSintesiRequest request) {
		operaService.modificaFlagSintesi(request);

		return ResponseEntity.ok().build();
	}
	
	
	@CrossOrigin(origins = "*") 
	@PostMapping("/modificaDescrizione")
	public ResponseEntity<?> modificaDescrizione(@RequestBody ModificaSintesiRequest request) {
		operaService.modificaSintesi(request);

		return ResponseEntity.ok().build();
	}
	
	
	

}