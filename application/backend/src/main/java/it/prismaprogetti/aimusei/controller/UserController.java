package it.prismaprogetti.aimusei.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.prismaprogetti.aimusei.model.Disabilita;
import it.prismaprogetti.aimusei.service.UserService;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private UserService service;

	@CrossOrigin(origins = "*") 
	@GetMapping("/getDescrizioneOpera")
	public ResponseEntity<String> getDescrizioneOpera(@RequestParam String tag) {
		return ResponseEntity.ok(service.getDescrizioneOpera(tag));
	}

	@CrossOrigin(origins = "*") 
	@GetMapping("/getSintesi")
	public ResponseEntity<String> getSintesiOpera(@RequestParam String tag, @RequestParam Disabilita disabilita) {
		return ResponseEntity.ok(service.getSintesi(tag, disabilita));
	}

	@CrossOrigin(origins = "*") 
	@GetMapping("/elabora")
	public ResponseEntity<String> elabora(@RequestParam String titolo,@RequestParam String disabilita) {
		return ResponseEntity.ok(service.elabora(titolo, disabilita));
	}
}
