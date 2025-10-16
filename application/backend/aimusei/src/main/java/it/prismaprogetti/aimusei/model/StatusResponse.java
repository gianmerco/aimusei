package it.prismaprogetti.aimusei.model;

import java.util.Map;
import lombok.Data;

@Data
public class StatusResponse {

	   private String tag;
	    private Integer version;
	    private String hashCode;
	    private String status; // INCOMPLETO, GENERATED, REVISIONED
	    private Map<String, String> details;
}