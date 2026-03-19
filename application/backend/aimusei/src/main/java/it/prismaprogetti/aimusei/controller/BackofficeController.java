package it.prismaprogetti.aimusei.controller;

import org.springframework.web.bind.annotation.RestController;


@RestController
public class BackofficeController {

//	@Autowired
//	private BackofficeService aiMuseiService;
//
//	@Autowired
//	private OperaService operaService;
//
//	@CrossOrigin(origins = "*") 
//	@PostMapping("/elaboraTestiDisabilita")
//	public ResponseEntity<Map<Disabilita, String>> elaboraTestiDisabilita(HttpServletRequest request,
//			@RequestBody ElaboraTestiDisabilitaRequest requestElaborazione)
//			throws BadRequestException, InvalidOpenAIKeyException {
//		return ResponseEntity.ok(aiMuseiService.elaboraTestiDisabilita(requestElaborazione));
//	}
//
//	@CrossOrigin(origins = "*") 
//	@GetMapping("/getOpera")
//	public ResponseEntity<?> getDescrizioneOpera(@RequestParam String tag) {
//		return ResponseEntity.ok(operaService.getOpera(tag));
//	}
//
//	@CrossOrigin(origins = "*") 
//	@PostMapping("/modificaSintesi")
//	public ResponseEntity<?> modificaSintesi(@RequestBody ModificaSintesiRequest request) {
//		operaService.modificaSintesi(request);
//
//		return ResponseEntity.ok().build();
//	}
//	
//	@CrossOrigin(origins = "*") 
//	@PostMapping("/modificaFlagSintesi")
//	public ResponseEntity<?> modificaFlagSintesi(@RequestBody ModificaFlagSintesiRequest request) {
//		operaService.modificaFlagSintesi(request);
//
//		return ResponseEntity.ok().build();
//	}
//	
//	
//	@CrossOrigin(origins = "*") 
//	@PostMapping("/modificaDescrizione")
//	public ResponseEntity<?> modificaDescrizione(@RequestBody ModificaSintesiRequest request) {
//		operaService.modificaSintesi(request);
//
//		return ResponseEntity.ok().build();
//	}

}