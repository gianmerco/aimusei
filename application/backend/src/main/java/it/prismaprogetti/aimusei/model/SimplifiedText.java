package it.prismaprogetti.aimusei.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplifiedText {
	   private TipoDisabilita tipo; // ADHD, DISLESSIA, DISCALCULIA, EASY_TO_READ
	    private String text;
	    private String generator;
	    private String validator;
	    private LocalDateTime dateInsert;
	    private LocalDateTime dateValidation;
	    private Boolean validate;
}