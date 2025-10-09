package it.prismaprogetti.aimusei.model;

import java.util.List;
import lombok.Data;

@Data
public class TextListRequest {
    private List<TagHashRequest> tagHashRequest;
}