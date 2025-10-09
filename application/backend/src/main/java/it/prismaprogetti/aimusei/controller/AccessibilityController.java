package it.prismaprogetti.aimusei.controller;

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

import it.prismaprogetti.aimusei.model.HashValidateRequest;
import it.prismaprogetti.aimusei.model.RegenerateSintesiRequest;
import it.prismaprogetti.aimusei.model.TextGeneratedRequest;
import it.prismaprogetti.aimusei.model.TextGeneratedResponse;
import it.prismaprogetti.aimusei.model.TextHashValidateRequest;
import it.prismaprogetti.aimusei.model.TextOriginalResponse;
import it.prismaprogetti.aimusei.service.AccessibilityService;

@RestController
@RequestMapping("/accessibility")
@CrossOrigin(origins = "*")
public class AccessibilityController {

	@Autowired
	private AccessibilityService accessibilityService;
	
	@GetMapping("/texts/getOriginalText")
	public ResponseEntity<TextOriginalResponse> getOriginalText(    @RequestParam String tag, 
		    @RequestParam String originalText) {
		TextOriginalResponse response = accessibilityService.getOriginalText(tag,originalText);
		return ResponseEntity.ok(response);
	} 

	@PostMapping("/texts/generate")
	public ResponseEntity<TextGeneratedResponse> generateSimplifiedTexts(@RequestBody TextGeneratedRequest request) {
		TextGeneratedResponse response = accessibilityService.generateSimplifiedTexts(request);
		return ResponseEntity.ok(response);
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