package it.prismaprogetti.aimusei.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponse {
    private int inserted;
    private int ko;
    private List<String> koTags;

}