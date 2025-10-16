package com.leonardo.aiservice;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to be used in AI applications where the user input must be processed using
 * one or more "contexts" (which might correspond to a specific system prompt or a
 * specific service).
 * The available contexts can be found in the Context enum class.
 */
public class AIRequest extends AbstractRequest {
    private List<Context> contexts;

    public AIRequest() {
        // il costruttore pubblico non va utilizzato, bisogna preferire il builder. E' tuttavia necessario lato AI Gateway che ne esista uno
        // non va utilizzato anche perché non ci sono setter
        super(null);
        this.contexts = null;
    }

    AIRequest(Builder builder) {
        // questo costruttore non è visibile, ma ci si accede indirettamente tramite Builder. L'utente finale non dovrebbe utilizzarlo
        super(builder.input);
        this.contexts = builder.contexts;
    }

    public List<Context> getContexts() {
        return contexts;
    }

    /**
     * Builder class to build AIRequest objects. It follows the typical builder pattern, having methods to provide the instance
     * variables of a AIRequest object.
     */
    public static class Builder {
        private List<Context> contexts = new ArrayList<>();
        private String input;

        /**
         * Add a list of Context objects to the ones the AIRequests has to be processed against
         * @param contexts a list of Context objects
         * @return the Builder instance
         */
        public Builder contexts(List<Context> contexts) {
            this.contexts.addAll(contexts);
            return this;
        }

        /**
         * Add a single Context objects to the ones the AIRequests has to be processed against
         * @param context a Context object
         * @return the Builder instance
         */
        public Builder context(Context context) {
            this.contexts.add(context);
            return this;
        }

        /**
         * Sets the AIRequest input to convert
         * @param input a string input
         * @return
         */
        public Builder input(String input) {
            this.input = input;
            return this;
        }

        /**
         * @return the AIRequest object, populated with the Contexts and the input provided using the other methods of the class
         */
        public AIRequest build() {
            return new AIRequest(this);
        }
    }

}