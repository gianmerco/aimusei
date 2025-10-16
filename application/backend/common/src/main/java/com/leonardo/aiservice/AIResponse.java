package com.leonardo.aiservice;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the AbstractResponse; it doesn't add additional instance variables.
 * Should be instanced using the Builder.
 */
public class AIResponse extends AbstractResponse {

    @SuppressWarnings("unused")
    AIResponse() { // similmente a AIRequest, questo construttore non deve e non può essere utilizzato, ma serve che esista per l'AI gateway
        super(new HashMap<>());
    }

    AIResponse(Builder builder) { // usare questo
            super(builder.outputs);
        }

    /**
     * Builder class to build AIResponse objects. It follows the typical builder pattern, having methods to provide the instance
     * variables of a AIResponse object.
     */
    public static class Builder {
        private Map<Context, Output<?>> outputs = new HashMap<>();

        /**
         * Add a collection of context - output pairs to the ones already stored in the response
         * @param outputs a map of context - output pairs
         * @return
         */
        public Builder outputs(Map<Context, Output<?>> outputs) {
            this.outputs.putAll(outputs);
            return this;
        }

        /**
         * Add a single context - output pair to the AIResponse
         * @param context a valid Context
         * @param output an Output object
         * @return
         */
        public Builder output(Context context, Output<?> output) {
            this.outputs.put(context, output);
            return this;
        }

        /**
         * @return the AIResponse object populated as determined by the other methods invocations
         */
        public AIResponse build() {
            return new AIResponse(this);
        }
    }
}
