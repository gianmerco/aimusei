package it.prismaprogetti.aimusei.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import it.prismaprogetti.aimusei.collection.OperaToInsert;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchPreleaver {
	
	@Autowired
    private MongoTemplate mongoTemplate;

	@Value("${preleaver.batch-size:50}")
	private int batchSize;
	
	public List<OperaToInsert> prelevaBatchUpdateProcessingAtomicoBulk() {
		
		 Query query = new Query(Criteria.where("status").is(OperaToInsert.Status.PENDING))
		            .with(Sort.by(Sort.Direction.ASC, "createdAt"))
		            .limit(batchSize);
		 
		 List<OperaToInsert> batch = mongoTemplate.find(query, OperaToInsert.class);
		 
		    if (!batch.isEmpty()) {
		        List<String> ids = batch.stream().map(OperaToInsert::getId).toList();
		        Update update = new Update()
		                .set("status", OperaToInsert.Status.PROCESSING)
		                .set("processingStartedAt", Instant.now());
		        mongoTemplate.updateMulti(
		                new Query(Criteria.where("_id").in(ids)),
		                update,
		                OperaToInsert.class
		        );
		    }
		    return batch;
	}

	public void portaBatchInError(List<OperaToInsert> batchAtomico) {
		try {
	    if (batchAtomico == null || batchAtomico.isEmpty()) return;

	    List<String> ids = batchAtomico.stream().map(OperaToInsert::getId).toList();
	    mongoTemplate.updateMulti(
	        Query.query(Criteria.where("_id").in(ids)),
				new Update().set("status", OperaToInsert.Status.ERROR), OperaToInsert.class
	    );
		} catch (Exception e) {
			log.error("Errore durante l'aggiornamento dello stato a ERROR per batch: {}", e.getMessage(), e);
		}
	}
}
