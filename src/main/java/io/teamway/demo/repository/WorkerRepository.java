package io.teamway.demo.repository;

import io.teamway.demo.model.Worker;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends CrudRepository<Worker, UUID> {

}
