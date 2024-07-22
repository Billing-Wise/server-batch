package site.billingwise.batch.server_batch.batch.listner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;


@Component
@Slf4j
public class JobCompletionCheckListener extends JobExecutionListenerSupport {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = LocalDateTime.now();
        log.info("Job started at: {}", startTime);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        endTime = LocalDateTime.now();
        log.info("Job ended at: {}",  endTime);

        Duration duration = Duration.between(startTime, endTime);
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        seconds = seconds % 60;

        log.info("Job duration: {} minutes and {} seconds", minutes, seconds);
        super.afterJob(jobExecution);
    }
}