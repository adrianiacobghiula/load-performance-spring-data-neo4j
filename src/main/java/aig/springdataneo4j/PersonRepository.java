package aig.springdataneo4j;

import aig.springdataneo4j.LoadTestApplication.Person;
import aig.springdataneo4j.LoadTestApplication.PersonSummary;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, String> {

  @Query(
      """
    MATCH (p:Person) WHERE p.name = $name
    OPTIONAL MATCH (p)-[p_k:KNOWS]->(k)
    RETURN p, collect(p_k) as p_k, collect(k) as k
    """)
  PersonSummary findByName(String name);
}
