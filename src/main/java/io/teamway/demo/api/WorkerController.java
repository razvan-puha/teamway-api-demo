package io.teamway.demo.api;

import io.teamway.demo.dto.WorkerDto;
import io.teamway.demo.dto.WorkerResponseDto;
import io.teamway.demo.service.WorkerService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

  private final WorkerService workerService;

  @Autowired
  public WorkerController(WorkerService workerService) {
    this.workerService = workerService;
  }

  @GetMapping
  public WorkerResponseDto getAllWorkers() {
    return workerService.getAllWorkers();
  }

  @GetMapping("/{workerId}")
  public WorkerResponseDto getWorker(@PathVariable String workerId) {
    return workerService.getWorker(UUID.fromString(workerId));
  }

  @PostMapping
  public WorkerResponseDto addWorker(@RequestBody WorkerDto worker) {
    return workerService.addWorker(worker);
  }
}
