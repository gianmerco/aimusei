package it.prismaprogetti.aimusei.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/public")
@Slf4j
@CrossOrigin(origins = "*")
public class PublicController {
	
//	@Autowired
//    private PublicService publicService;
    
//    @GetMapping("/texts/{tag}/latest")
//    public ResponseEntity<?> getLatestTextVersion(
//            @PathVariable String tag,
//            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
//        
//        Map<String, Object> response = publicService.getLatestTextVersion(tag, 
//			acceptLanguage);
//        return ResponseEntity.ok(response);
//    }

	
	
	
	
//    @PostMapping("/texts/massive")
//    public ResponseEntity<MassiveTextsResponse> getMassiveTexts(
//            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
//            @RequestHeader(value = "X-TipoSemplificato", required = false) TipoDisabilita xTipoSemplificato,
//            @Valid @RequestBody TextListRequest request) {
//        	
//    	MassiveTextsResponse response = publicService.getMassiveTexts(request.getTagHashRequest(),xTipoSemplificato,acceptLanguage);
//        return ResponseEntity.ok(response);
//    }
//    
}