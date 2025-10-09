package it.prismaprogetti.aimusei.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.prismaprogetti.aimusei.service.CacheService;

@RestController
@RequestMapping("/cache")
public class CacheController {
    
    @Autowired
    private CacheService cacheService;
    
    @PostMapping("/texts/cache/check")
    public ResponseEntity<?> checkCacheByHash(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization") String authorization) {
        
        Map<String, Object> response = cacheService.checkCacheByHash(request);
        return ResponseEntity.ok(response);
    }
}