package site.billingwise.batch.server_batch.batch.listner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class JobCompletionCheckListener extends JobExecutionListenerSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = LocalDateTime.now();
        log.info("Job 시작:{}", jobExecution.getJobInstance().getJobName());
        log.info("Job 시작 시간 : {}", startTime);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        endTime = LocalDateTime.now();
        log.info("Job 종료:{}", jobExecution.getJobInstance().getJobName());
        log.info("종료 시간: {}", endTime);

        Duration duration = Duration.between(startTime, endTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        log.info("지속 시간: {}분 {}초", minutes, seconds);


        BatchStatus jobStatus = jobExecution.getStatus();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        log.info("Job 상태: {}, 결과: {}", jobStatus, jobExitStatus);

        if (jobStatus == BatchStatus.FAILED) {
            log.error("Job 실패: {}", jobExecution.getJobInstance().getJobName());
            jobExecution.getAllFailureExceptions().forEach(exception -> {
                log.error("Job 예외 발생: {}", exception.getMessage(), exception);
            });
        }


        long totalReadCount = jobExecution.getStepExecutions()
                .stream()
                .mapToLong(StepExecution::getReadCount)
                .sum();

        long totalWriteCount = jobExecution.getStepExecutions()
                .stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();

        long totalSkipCount = jobExecution.getStepExecutions()
                .stream()
                .mapToLong(StepExecution::getSkipCount)
                .sum();

        long totalFailureCount = jobExecution.getStepExecutions()
                .stream()
                .filter(stepExecution -> stepExecution.getStatus() == BatchStatus.FAILED)
                .count();

        log.info("Job 처리 결과 - 총 읽기 건수: {}, 총 성공 건수: {}, 총 스킵 건수: {}, 총 실패 Step 수: {}",
                totalReadCount, totalWriteCount, totalSkipCount, totalFailureCount);

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("jobName", jobExecution.getJobInstance().getJobName());
        logMap.put("status", jobExecution.getStatus().toString());
        logMap.put("exitStatus", jobExecution.getExitStatus().getExitCode());
        logMap.put("startTime", startTime.toString());
        logMap.put("endTime", endTime.toString());
        logMap.put("duration", String.format("%d minutes and %d seconds", minutes, seconds));
        logMap.put("totalReadCount", totalReadCount);
        logMap.put("totalWriteCount", totalWriteCount);
        logMap.put("totalSkipCount", totalSkipCount);
        logMap.put("totalFailureCount", totalFailureCount);

        try {
            String jsonLog = objectMapper.writeValueAsString(logMap);
            log.info("Job 종료 로그: {}", jsonLog);
        } catch (JsonProcessingException e) {
            log.error("json 변환 중 에러 발생 ", e);
        }


        super.afterJob(jobExecution);
    }
}