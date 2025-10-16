package it.prismaprogetti.aimusei.model;

import lombok.Data;

@Data
public class TagHashRequest {
    private String tag;
    private String hash;
}