package it.prismaprogetti.aimusei.controller;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.prismaprogetti.aimusei.model.GenerateImageRequest;
import it.prismaprogetti.aimusei.model.HashValidateRequest;
import it.prismaprogetti.aimusei.model.ImageToTextRequest;
import it.prismaprogetti.aimusei.model.ImageToTextResponse;
import it.prismaprogetti.aimusei.model.RegenerateSintesiRequest;
import it.prismaprogetti.aimusei.model.TextGeneratedRequest;
import it.prismaprogetti.aimusei.model.TextGeneratedResponse;
import it.prismaprogetti.aimusei.model.TextHashValidateRequest;
import it.prismaprogetti.aimusei.model.TextOriginalResponse;
import it.prismaprogetti.aimusei.service.AccessibilityService;
import it.prismaprogetti.aimusei.service.PDFService;
import it.prismaprogetti.aimusei.service.SintesiService;

@RestController
@RequestMapping("/accessibility")
@CrossOrigin(origins = "*")
public class AccessibilityController {

	@Autowired
	private AccessibilityService accessibilityService;
	@Autowired
	private SintesiService sintesiService;
	@Autowired
	private PDFService pdfService;
	
	// TODO assicurarsi della unicità del tag, due musei diversi che utilizzando uno
	// stesso tag vanno in conflitto. Valutare se passare insieme anche id museo
	@GetMapping("/texts/getOriginalText")
	public ResponseEntity<TextOriginalResponse> getOriginalText(    @RequestParam String tag, 
		    @RequestParam String originalText) {
		TextOriginalResponse response = accessibilityService.getOriginalText(tag,originalText);
		return ResponseEntity.ok(response);
	}
	
	
	@GetMapping("/texts/getStatus")
	public ResponseEntity<TextStatusResponse> getStatus(    @RequestParam String tag, 
		    @RequestParam String originalText) {
		TextStatusResponse response = accessibilityService.getTextStatus(tag,originalText);
		return ResponseEntity.ok(response);
	} 

	@PostMapping("/texts/generate")
	public ResponseEntity<TextGeneratedResponse> generateSimplifiedTexts(@RequestBody TextGeneratedRequest request) {
		TextGeneratedResponse response = accessibilityService.generateSimplifiedTexts(request);
		return ResponseEntity.ok(response);
	}
	
	//TODO deve arrivare in input anche l'id museo per il salvataggio su s3
	@PostMapping("/texts/generateImage")
	public ResponseEntity<byte[]> generateImage(@RequestBody GenerateImageRequest request) {
		return ResponseEntity.ok(accessibilityService.generateImage(request));
	}
	
	@GetMapping("/texts/getImageUrl")
	public ResponseEntity<URL> getImageUrl( @RequestParam String idMuseo) {
		return ResponseEntity.ok(accessibilityService.getImageUrl(idMuseo));
	}
	
	@GetMapping("/texts/getImage")
	public ResponseEntity<byte[]> getImage( @RequestParam String idMuseo) {
		return ResponseEntity.ok(accessibilityService.getImage(idMuseo));
	}
	
	@GetMapping("/texts/imageExists")
	public ResponseEntity<Boolean> imageExists( @RequestParam String idMuseo) {
		return ResponseEntity.ok(accessibilityService.imageExists(idMuseo));
	}
	
	@PostMapping("/images/generateText")
	public ResponseEntity<ImageToTextResponse> generateTextFromImage(@RequestBody ImageToTextRequest request) {
	    ImageToTextResponse response = accessibilityService.generateTextFromImage(request);
	    return ResponseEntity.ok(response);
	}
	
	
//	@PostMapping("/texts/generateImage")
//	public ResponseEntity<byte[]> generateImage() {
//		byte[] response = pdfService.generateImagesMock();
//		
//		  // Prepara headers per download
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDispositionFormData("attachment", "immagini.pdf");
//        headers.setContentLength(response.length);
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(response);
//	}
	
	@PostMapping("/active-ai-service")
	public ResponseEntity<?> activeAiService(@RequestBody Boolean activate) {
		sintesiService.activeAiService(activate);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/active-ai-service")
	public ResponseEntity<Boolean> isActiveAiService() {
		return ResponseEntity.ok(sintesiService.isActiveAiService());
	}

//	@GetMapping("/texts/status/{tag}")
//	public ResponseEntity<?> getTextStatus(@PathVariable String tag,
//			@RequestHeader(value = "Authorization") String authorization) {
//		StatusResponse response = accessibilityService.getTextStatus(tag);
//		return ResponseEntity.ok(response);
//	}

	@PutMapping("/texts/revise")
	public ResponseEntity<?> reviseText(@RequestBody TextHashValidateRequest request) {
		accessibilityService.reviseText(request);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/texts/validate")
	public ResponseEntity<?> validateText(@RequestBody HashValidateRequest request) {
		accessibilityService.validateText(request);
		return ResponseEntity.ok().build();
	}
	
	@PatchMapping("/texts/regenerateSintesi")
	public ResponseEntity<?> regenerateSintesi(@RequestBody RegenerateSintesiRequest request) {
		return ResponseEntity.ok(accessibilityService.regenerateSintesi(request));
	}
}