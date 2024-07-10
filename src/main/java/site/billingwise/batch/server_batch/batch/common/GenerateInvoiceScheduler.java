package site.billingwise.batch.server_batch.batch.common;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class GenerateInvoiceScheduler {

    private static final Logger logger = LoggerFactory.getLogger(GenerateInvoiceScheduler.class);
    private final JobLauncher jobLauncher;
    private final Job generateInvoiceJob;
    private final Job jdbcGenerateInvoiceJob;

    //    일단 0,30초마다 실행하도록 실행
    @Scheduled(cron = "0,30 * * * * ?")
    public void generateInvoice() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("invoice", System.currentTimeMillis())
                .toJobParameters();
        try {
            logger.info("JPA 배치 프로그램 실행 시작");
            jobLauncher.run(generateInvoiceJob, jobParameters);
            logger.info("JPA 배치 프로그램 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            logger.error("JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            logger.error("JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            logger.error("JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            logger.error("JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            logger.error("예기치 않은 오류 발생: ", e);
        }
    }


    //    일단 15,45초마다 실행하도록 실행
    @Scheduled(cron = "15,45 * * * * ?")
    public void jdbcgenerateInvoice() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("jdbcInvoice", System.currentTimeMillis())
                .toJobParameters();
        try {
            logger.info("JDBC 배치 프로그램 실행 시작");
            jobLauncher.run(jdbcGenerateInvoiceJob, jobParameters);
            logger.info("JDBC 배치 프로그램 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            logger.error("JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            logger.error("JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            logger.error("JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            logger.error("JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            logger.error("예기치 않은 오류 발생: ", e);
        }
    }
}

