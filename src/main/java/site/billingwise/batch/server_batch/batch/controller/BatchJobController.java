package site.billingwise.batch.server_batch.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job jdbcGenerateInvoiceJob;
    private final Job invoiceProcessingJob;
    private final Job weeklyInvoiceStatisticsJob;
    private final Job monthlyInvoiceStatisticsJob;

    @PostMapping("/generate-invoice")
    public String runJdbcGenerateInvoiceJob() {
        return launchJob(jdbcGenerateInvoiceJob, "jdbcInvoice");
    }

    @PostMapping("/process-invoice")
    public String runInvoiceProcessingJob() {
        return launchJob(invoiceProcessingJob, "InvoiceProcessingJob");
    }

    @PostMapping("/weekly-statistics")
    public String runWeeklyStatisticsJob() {
        return launchJob(weeklyInvoiceStatisticsJob, "weeklyInvoiceStatisticsJob");
    }

    @PostMapping("/monthly-statistics")
    public String runMonthlyStatisticsJob() {
        return launchJob(monthlyInvoiceStatisticsJob, "monthlyInvoiceStatisticsJob");
    }



    private String launchJob(Job job, String jobName) {
        String uuid = UUID.randomUUID().toString();
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong(jobName, System.currentTimeMillis())
                .addString("UUID", uuid)
                .toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
            return "Job " + jobName + " submitted successfully. UUID: " + uuid;
        } catch (Exception e) {
            log.error("Error occurred while starting job: {} with UUID: {}. Error: {}", jobName, uuid, e.getMessage());
            return "Error occurred while starting job: " + jobName + ". UUID: " + uuid;
        }
    }
}
