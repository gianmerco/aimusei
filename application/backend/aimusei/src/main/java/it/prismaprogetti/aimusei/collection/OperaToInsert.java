package it.prismaprogetti.aimusei.collection;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "opera_to_insert")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OperaToInsert {

	@Id
	private String id;
	private String tag;
	private String descrizione;
	private String jobId;
	private Status status;
	@Builder.Default
	private Instant createdAt = Instant.now();
	private Instant processingStartedAt;
	
	public enum Status {
		PENDING, PROCESSING, GENERATED, ERROR
	}
}