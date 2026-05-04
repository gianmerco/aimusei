package it.prismaprogetti.aimusei.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import it.prismaprogetti.aimusei.collection.Job;
import it.prismaprogetti.aimusei.collection.OperaToInsert;
import it.prismaprogetti.aimusei.model.StatoJob;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobsUpdaterService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void aggiornaStatoJobs() {
        Query jobQuery = new Query(Criteria.where("stato").is(StatoJob.PENDING));
        List<Job> jobs = mongoTemplate.find(jobQuery, Job.class);

        for (Job job : jobs) {
            try {
                aggiornaSingoloJob(job);
            } catch (Exception e) {
                log.error("Errore aggiornamento job {}: {}", job.getId(), e.getMessage(), e);
                // continua con il prossimo job
            }
        }
    }

    private void aggiornaSingoloJob(Job job) {
        // 1) Recupera i tag raggruppati per stato
        List<String> pendingTags = mongoTemplate.findDistinct(
            new Query(Criteria.where("jobId").is(job.getId())
                    .and("status").is(OperaToInsert.Status.PENDING)),
            "tag", OperaToInsert.class, String.class);

        List<String> processingTags = mongoTemplate.findDistinct(
            new Query(Criteria.where("jobId").is(job.getId())
                    .and("status").is(OperaToInsert.Status.PROCESSING)),
            "tag", OperaToInsert.class, String.class);

        List<String> generatedTags = mongoTemplate.findDistinct(
            new Query(Criteria.where("jobId").is(job.getId())
                    .and("status").is(OperaToInsert.Status.GENERATED)),
            "tag", OperaToInsert.class, String.class);

        List<String> errorTags = mongoTemplate.findDistinct(
            new Query(Criteria.where("jobId").is(job.getId())
                    .and("status").is(OperaToInsert.Status.ERROR)),
            "tag", OperaToInsert.class, String.class);

        // 2) Aggiorna il documento Job con le liste e stabilisci lo stato finale
        Update update = new Update()
                .set("pendingTags", pendingTags)
                .set("processingTags", processingTags)
                .set("generatedTags", generatedTags)
                .set("errorTags", errorTags)
                .set("lastUpdated", Instant.now());

        // Se non ci sono più opere in PENDING o PROCESSING, il job è terminato
        if (pendingTags.isEmpty() && processingTags.isEmpty()) {
            StatoJob statoFinale = errorTags.isEmpty() 
                    ? StatoJob.COMPLETATO 
                    : StatoJob.COMPLETATO_CON_ERRORI;
            update.set("stato", statoFinale);
        }

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(job.getId())),
                update,
                Job.class);
    }
}