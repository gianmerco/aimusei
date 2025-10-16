package it.prismaprogetti.aimusei.model;

import lombok.Getter;

@Getter
public enum Disabilita {

	DISCALCULIA(
			". Adatta questo testo ad una persona affetta da discalculia, escludi dunque date, formule e numeri in generale."),
	DISLESSIA(
			". Adatta questo testo ad una persona affetta da dislessia. Usa un linguaggio e periodi di facile comprensione."),
	EASY_TO_READ(
			". Adatta questo testo ad una persona affetta da ADHD. Usa un linguaggio e periodi di facile comprensione."),
	DISCALCULIA_NEW(
			". Adatta questo testo ad una persona affetta da discalculia, escludi dunque date, formule e numeri in generale."),
	DISLESSIA_NEW(
			". Adatta questo testo ad una persona affetta da dislessia. Usa un linguaggio e periodi di facile comprensione."),
	EASY_TO_READ_NEW(
			". Adatta questo testo ad una persona affetta da ADHD. Usa un linguaggio e periodi di facile comprensione.");

	private final String prompt;

	private Disabilita(String prompt) {
		this.prompt = prompt + " Rispondi esclusivamente col testo";
	}

}