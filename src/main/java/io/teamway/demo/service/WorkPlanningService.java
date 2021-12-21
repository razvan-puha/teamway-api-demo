package io.teamway.demo.service;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import io.teamway.demo.dto.WorkPlanResponseDto;
import io.teamway.demo.dto.WorkerDto;
import io.teamway.demo.dto.WorkerResponseDto;
import io.teamway.demo.dto.WorkerShiftDto;
import io.teamway.demo.model.ShiftHistory;
import io.teamway.demo.model.ShiftType;
import io.teamway.demo.repository.ShiftHistoryRepository;
import io.teamway.demo.util.Constants;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkPlanningService {

  public static final int MAX_SHIFTS = 5;
  private final WorkerService workerService;
  private final ShiftHistoryRepository shiftHistoryRepository;

  @Autowired
  public WorkPlanningService(WorkerService workerService,
      ShiftHistoryRepository shiftHistoryRepository) {
    this.workerService = workerService;
    this.shiftHistoryRepository = shiftHistoryRepository;
  }

  @Transactional(propagation = REQUIRES_NEW)
  public WorkPlanResponseDto generateWeeklyWorkPlan(LocalDate startDate) {
    Map<UUID, Integer> workerShiftsMap = createWorkerShiftMap(workerService.getAllWorkers());
    return WorkPlanResponseDto.builder()
        .workPlan(startDate.datesUntil(startDate.plus(1, ChronoUnit.WEEKS),
                Period.ofDays(1))
                          .map(it -> createShiftForDay(it, workerShiftsMap))
                          .flatMap(List::stream)
                          .collect(Collectors.toList())
                )
        .build();
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void saveWorkPlan(List<WorkerShiftDto> workPlan) {
    shiftHistoryRepository.saveAll(workPlan.stream()
        .map(this::createShiftHistoryEntity)
        .collect(Collectors.toList()));
  }

  /**
   * This will create a map containing the workers and the no. of shifts they have in a week. (To
   * simulate a real-life situation, a worker can have {@value #MAX_SHIFTS} shifts per week)
   *
   * @param allWorkers
   * @return
   */
  private Map<UUID, Integer> createWorkerShiftMap(WorkerResponseDto allWorkers) {
    Map<UUID, Integer> resultMap = new HashMap<>();
    allWorkers.getResponse().stream()
        .map(WorkerDto::getId)
        .forEach(it -> resultMap.put(it, 0));
    return resultMap;
  }

  /**
   * Very simple shift assignment algorithm. It chooses randomly one worker and assign a shift for
   * the mentioned date.
   *
   * @param date
   * @param workerShifts
   * @return
   */
  private List<WorkerShiftDto> createShiftForDay(LocalDate date, Map<UUID, Integer> workerShifts) {
    List<WorkerShiftDto> shifts = new ArrayList<>();
    List<ShiftType> shiftTypes = new ArrayList<>(List.of(ShiftType.values()));
    List<UUID> workers = new ArrayList<>(workerShifts.keySet());
    Map<UUID, Boolean> workerHasAShiftMap = new HashMap<>();

    while (!shiftTypes.isEmpty()) {
      UUID worker = workers.get((int) (Math.random() * workers.size()));
      if (BooleanUtils.isNotTrue(workerHasAShiftMap.get(worker)) &&
          workerShifts.get(worker) < MAX_SHIFTS) {
        ShiftType shiftType = shiftTypes.remove((int) (Math.random() * shiftTypes.size()));
        shifts.add(WorkerShiftDto.builder()
            .shiftDate(date.format(Constants.DATE_FORMATTER))
            .shiftType(shiftType.name())
            .workerId(worker)
            .build());
        workerHasAShiftMap.put(worker, Boolean.TRUE);
      }
    }
    return shifts;
  }

  private ShiftHistory createShiftHistoryEntity(WorkerShiftDto workerShiftDto) {
    ShiftHistory shiftHistory = new ShiftHistory();
    shiftHistory.setWorkerId(workerShiftDto.getWorkerId());
    shiftHistory.setShiftType(ShiftType.valueOf(workerShiftDto.getShiftType()));
    shiftHistory.setWorkedDate(LocalDate.parse(workerShiftDto.getShiftDate(), Constants.DATE_FORMATTER));
    return shiftHistory;
  }
}
