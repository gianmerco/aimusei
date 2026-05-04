package it.prismaprogetti.aimusei.collection;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import it.prismaprogetti.aimusei.model.StatoJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "job")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Job {
	@Id
	private String id;
	private LocalDateTime creationDateTime;
	private StatoJob stato;
	private Integer opereParsed;
	private List<String> pendingTags;
	private List<String> processingTags;
	private List<String> generatedTags;
	private List<String> errorTags;
	private Instant lastUpdated;
}