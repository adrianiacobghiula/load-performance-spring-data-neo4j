package aig.springdataneo4j;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@SpringBootApplication
public class LoadTestApplication {

  private static final Logger log = LoggerFactory.getLogger(LoadTestApplication.class);

  @Node("Person")
  @AllArgsConstructor
  @Getter
  @Setter
  public static class Person {

    @Id String name;

    @Relationship("KNOWS")
    List<Person> persons;
  }

  public interface PersonSummary {

    String getName();

    List<PersonSimplified> getPersons();

    interface PersonSimplified {
      String getName();
    }
  }

  @Bean
  Configuration cypherDslConfiguration() {
    return Configuration.newConfig()
        .withDialect(Dialect.NEO4J_5).build();
  }


  public static void main(String[] args) {

    /*
    CREATE (p:Person {id: 0, name: 'name0'})
    WITH p
    UNWIND range(1, 10000, 1) as id
    CREATE (k:Person {id: id, name: 'name'+id})
    CREATE (p)-[:KNOWS]->(k)
     */

    try (ConfigurableApplicationContext ctx =
        SpringApplication.run(LoadTestApplication.class, args)) {
      PersonRepository repo = ctx.getBean(PersonRepository.class);

      testUsingFindById(repo);
      testUsingFindByName(repo);

    } catch (Exception e) {
      log.error("unexpected exception", e);
    }
  }

  private static void testUsingFindById(PersonRepository repo) {
    long startTime = System.currentTimeMillis();
    repo.findById("name0")
        .ifPresent(
            a ->
                log.info(
                    "findById - Read {} with {} relations in {} msec",
                    a.getName(),
                    a.getPersons().size(),
                    (System.currentTimeMillis() - startTime)));
  }

  private static void testUsingFindByName(PersonRepository repo) {
    long startTime = System.currentTimeMillis();
    PersonSummary a = repo.findByName("name0");

    log.info(
        "findByName - Read {} with {} relations in {} msec",
        a.getName(),
        a.getPersons().size(),
        (System.currentTimeMillis() - startTime));
  }
}
