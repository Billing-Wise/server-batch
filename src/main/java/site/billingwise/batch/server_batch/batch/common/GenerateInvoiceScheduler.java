package site.billingwise.batch.server_batch.batch.common;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import site.billingwise.batch.server_batch.domain.invoice.repository.InvoiceRepository;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class GenerateInvoiceScheduler {

    private final JobLauncher jobLauncher;
    private final Job generateInvoiceJob;

    //일단 30초마다 실행하도록 실행
    @Scheduled(cron = "0,30 * * * * ?")
    public void generateInvoice() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("invoice", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(generateInvoiceJob,jobParameters);
        }catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }

}
