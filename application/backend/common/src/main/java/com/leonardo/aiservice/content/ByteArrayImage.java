package com.leonardo.aiservice.content;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * rappresenta un contenuto costituito da una singola immagine rappresentata come un byte array
*/

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ByteArrayImage implements ImageContent {
    private final byte[] value;

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ByteArrayImage other)) return false;
        return Arrays.equals(this.value, other.value);
    }

    // Avoid printing the entire byte array
    @Override
    public String toString() {
        return "ImageContent(size=" + (value != null ? value.length : 0) + " bytes)";
    }
}
