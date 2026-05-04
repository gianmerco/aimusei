package it.prismaprogetti.aimusei.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.prismaprogetti.aimusei.collection.OperaToInsert;

public interface OperaToInsertRepository extends MongoRepository<OperaToInsert, String> {
    
}