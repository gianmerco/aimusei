package it.prismaprogetti.aimusei.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import it.prismaprogetti.aimusei.collection.OperaToInsert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CleanerService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${cleaner.timeout-hours:4}")
    private int timeoutHours;

    public void pulisciOpereBloccate() {
        try {
            Instant scadenza = Instant.now().minus(Duration.ofHours(timeoutHours));

            Query query = new Query(Criteria.where("status").is(OperaToInsert.Status.PROCESSING)
                    .and("processingStartedAt").lt(scadenza));

            Update update = new Update()
                    .set("status", OperaToInsert.Status.ERROR);

            mongoTemplate.updateMulti(query, update, OperaToInsert.class);
        } catch (Exception e) {
            log.error("Errore durante la pulizia delle opere bloccate", e);
        }
    }
}