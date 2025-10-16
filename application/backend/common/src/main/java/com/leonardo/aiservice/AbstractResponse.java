package com.leonardo.aiservice;


import java.util.Map;

/**
 * A generic response to an interrogation to the AIService.
 * For now, the output is a map of Context objects and instances of the generic Output class,
 * which can represent outputs of different type.
 * This implementation is sufficient for the current applications of the service. In this way it can
 * answer both AIRequests and other "normal" request by using Context.NONE.
 * Objects of the subclasses have to be instantiated via the correspondent builder.
 */
public abstract class AbstractResponse {
    protected Map<Context, Output<?>> output;

    protected AbstractResponse(Map<Context, Output<?>> output) {
        this.output = output;
    }

    /**
     * @return the response actual value, as a map
     */
    public Map<Context, Output<?>> getOutput() {
        return output;
    }

}