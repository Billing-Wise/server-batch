package site.billingwise.batch.server_batch.batch.listner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StepCompletionCheckListener extends StepExecutionListenerSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LocalDateTime stepStartTime;
    private LocalDateTime stepEndTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepStartTime = LocalDateTime.now();
        log.info("Step 시작: {}", stepExecution.getStepName());
        log.info("Step 시작 시간 : {}", stepStartTime);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        stepEndTime = LocalDateTime.now();
        log.info("Step 종료: {}", stepExecution.getStepName());
        log.info("Step 종료 시간: {}", stepEndTime);

        Duration duration = Duration.between(stepStartTime, stepEndTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        log.info("Step 지속 시간: {}분 {}초", minutes, seconds);


        // Step 상태 정보
        ExitStatus exitStatus = stepExecution.getExitStatus();
        BatchStatus batchStatus = stepExecution.getStatus();

        log.info("Step 종료: {}", stepExecution.getStepName());


        // 각 Step에서의 처리 결과
        long totalReadCount = stepExecution.getReadCount();
        long totalWriteCount = stepExecution.getWriteCount();
        long totalSkipCount = stepExecution.getSkipCount();
        long commitCount = stepExecution.getCommitCount();


        log.info("Step 처리 결과 - 상태: {}, 결과: {}, 총 읽기 건수: {}, 총 쓰기 건수: {}, 커밋 건수: {}, 스킵 건수: {}",
                batchStatus, exitStatus, totalReadCount, totalWriteCount, commitCount, totalSkipCount);

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("stepName", stepExecution.getStepName());
        logMap.put("status", batchStatus.toString());
        logMap.put("exitStatus", exitStatus.getExitCode());
        logMap.put("startTime", stepStartTime.toString());
        logMap.put("endTime", stepEndTime.toString());
        logMap.put("duration", String.format("%d minutes and %d seconds", minutes, seconds));
        logMap.put("totalReadCount", totalReadCount);
        logMap.put("totalWriteCount", totalWriteCount);
        logMap.put("totalSkipCount", totalSkipCount);
        logMap.put("commitCount", commitCount);


        try {
            String jsonLog = objectMapper.writeValueAsString(logMap);
            log.info("Step 종료 로그: {}", jsonLog);
        } catch (JsonProcessingException e) {
            log.error("json 변환 중 에러 발생 ", e);
        }


        // Step이 실패한 경우
        if (batchStatus == BatchStatus.FAILED) {
            log.error("Step 실패: {}", stepExecution.getStepName());
            stepExecution.getFailureExceptions().forEach(exception ->
                    log.error("Step 예외 발생: {}", exception.getMessage(), exception));
        }


        return exitStatus;
    }
}


