package it.prismaprogetti.aimusei.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.prismaprogetti.aimusei.collection.Job;

public interface JobRepository extends MongoRepository<Job, String> {
    
}