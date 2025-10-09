package it.prismaprogetti.aimusei.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import it.prismaprogetti.aimusei.collection.Opera;

public interface OperaRepository extends MongoRepository<Opera, String> {
    
    @Query(value = "{ 'tag' : ?0, 'latest' : true }", fields = "{ 'descrizione' : 1, '_id' : 0 }")
    Optional<String> findDescrizioneByNome(String nome);

    @Query(value = "{ 'tag' : ?0, 'latest' : true, 'sintesi' : { $elemMatch: { 'disabilita' : ?1, 'validata' : true } } }", fields = "{ 'sintesi.$' : 1 }")
    Optional<String> findSintesiByNome(String nome, String disabilita);

    @Query("{ 'tag' : ?0, 'latest' : true }")
    Optional<Opera> findByTag(String tag);
    
    Optional<Opera> findByHash(String hash);
}