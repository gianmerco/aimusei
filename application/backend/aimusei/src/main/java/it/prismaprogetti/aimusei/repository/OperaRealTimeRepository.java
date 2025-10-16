package it.prismaprogetti.aimusei.repository;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import it.prismaprogetti.aimusei.collection.Opera;
import it.prismaprogetti.aimusei.collection.OperaRealTime;

public interface OperaRealTimeRepository extends MongoRepository<OperaRealTime, String> {
	@Query(value = "{ 'tag' : ?0 }", fields = "{ 'descrizione' : 1, '_id' : 0 }")
	Optional<String> findDescrizioneByNome(String nome);

	@Query(value = "{ 'tag' : ?0, 'sintesi' : { $elemMatch : { 'disabilita' : ?1, 'validata' : true } } }", fields = "{ 'sintesi.$' : 1 }")
	Optional<String> findSintesiByNome(String nome, String disabilita);

	@Query("{'nome': ?0}")
	Optional<Opera> findByTag(String nome);

}
