package it.prismaprogetti.aimusei.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.prismaprogetti.aimusei.model.MassiveTextsResponse;
import it.prismaprogetti.aimusei.model.TextListRequest;
import it.prismaprogetti.aimusei.model.TipoDisabilita;
import it.prismaprogetti.aimusei.service.PublicService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {
	
	@Autowired
    private PublicService publicService;
    
//    @GetMapping("/texts/{tag}/latest")
//    public ResponseEntity<?> getLatestTextVersion(
//            @PathVariable String tag,
//            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
//        
//        Map<String, Object> response = publicService.getLatestTextVersion(tag, 
//			acceptLanguage);
//        return ResponseEntity.ok(response);
//    }
    
    @PostMapping("/texts/massive")
    public ResponseEntity<MassiveTextsResponse> getMassiveTexts(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @RequestHeader(value = "X-TipoSemplificato", required = false) TipoDisabilita xTipoSemplificato,
            @Valid @RequestBody TextListRequest request) {
        	
    	MassiveTextsResponse response = publicService.getMassiveTexts(request.getTagHashRequest(),xTipoSemplificato,acceptLanguage);
        return ResponseEntity.ok(response);
    }
    
}