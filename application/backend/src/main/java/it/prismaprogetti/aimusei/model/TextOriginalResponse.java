package it.prismaprogetti.aimusei.model;

import it.prismaprogetti.aimusei.collection.Opera;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TextOriginalResponse extends TextGeneratedResponse {

    private boolean hashMatch; 
    
    // Costruttore che chiama il super e setta hashMatch
    public TextOriginalResponse(TextGeneratedResponse generatedResponse, boolean hashMatch) {
        super(generatedResponse.getEngineLLM(), 
              generatedResponse.getTextVersion(), 
              generatedResponse.getHashCode(), 
              generatedResponse.getTitle(), 
              generatedResponse.getTextSimplified());
        this.hashMatch = hashMatch;
    }
    
    public static TextOriginalResponse fromOpera(Opera opera, boolean hashMatch) {
        TextGeneratedResponse generatedResponse = TextGeneratedResponse.fromOpera(opera);
        return new TextOriginalResponse(generatedResponse, hashMatch);
    }
}