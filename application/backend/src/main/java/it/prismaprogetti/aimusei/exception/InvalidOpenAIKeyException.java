package it.prismaprogetti.aimusei.exception;

public class InvalidOpenAIKeyException extends Exception {

	public InvalidOpenAIKeyException(String string) {
		super(string);
	}
	
	public InvalidOpenAIKeyException(String string , Throwable cause) {
		super(string,cause);
	}
}
