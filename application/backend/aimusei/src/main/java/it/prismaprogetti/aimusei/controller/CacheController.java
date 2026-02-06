package it.prismaprogetti.aimusei.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
@CrossOrigin(origins = "*")
public class CacheController {
//    
//    @Autowired
//    private CacheService cacheService;
//    
//    @PostMapping("/texts/cache/check")
//    public ResponseEntity<?> checkCacheByHash(
//            @RequestBody Map<String, String> request,
//            @RequestHeader(value = "Authorization") String authorization) {
//        
//        Map<String, Object> response = cacheService.checkCacheByHash(request);
//        return ResponseEntity.ok(response);
//    }
}