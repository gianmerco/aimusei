package com.leonardo.aiservice;

/**
 * Represent a generic output from a request. Since the output could
 * be of different types, the class is generic.
 * @param <T> the actual type of the output
 */
public class Output<T> {
    private final T value;

    @SuppressWarnings("unused")
    Output() { // l'utente non costruisce Outputs; necessario però questo costruttore per deserializzare lato AI Gateway
        this.value = null;
    }

    public Output(T value) {
        this.value = value;
    }

    /**
     * @return the actual value of the output
     */
    public T getValue() {
        return value;
    }
}