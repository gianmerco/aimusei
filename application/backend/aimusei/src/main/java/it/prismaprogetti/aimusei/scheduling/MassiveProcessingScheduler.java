package it.prismaprogetti.aimusei.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import it.prismaprogetti.aimusei.service.CleanerService;
import it.prismaprogetti.aimusei.service.JobsUpdaterService;
import it.prismaprogetti.aimusei.service.MassiveService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MassiveProcessingScheduler {
	
	@Autowired
	private MassiveService massiveService;
	@Autowired
	private JobsUpdaterService jobsUpdaterService;
	@Autowired
	private CleanerService cleanerService;

	 // Scheduler Operaio
    @Scheduled(fixedDelayString = "${scheduler.opere.delay-ms:10000}")
    public void processaOpere() {
    	try {
    	massiveService.processaOpere();
    	} catch (Exception e) {
            log.error("Errore nello scheduler processaOpere", e);
		}
    }

    // Scheduler Supervisore
    @Scheduled(fixedDelayString = "${scheduler.jobs.delay-ms:60000}")
    public void aggiornaStatoJobs() {
    	try {
        	jobsUpdaterService.aggiornaStatoJobs();
        	} catch (Exception e) {
                log.error("Errore nello scheduler aggiornaStatoJobs", e);
    		}
    }

    // Scheduler Pulitore
    @Scheduled(fixedDelayString = "${scheduler.pulitore.delay-ms:120000}")
    public void pulisciOpereBloccate() {
    	try {
        	cleanerService.pulisciOpereBloccate();
        	} catch (Exception e) {
                log.error("Errore nello scheduler pulisciOpereBloccate", e);
    		}
    }
	
}