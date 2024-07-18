package site.billingwise.batch.server_batch.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class Scheduler {

    private final JobLauncher jobLauncher;
    private final Job generateInvoiceJob;
    private final Job jdbcGenerateInvoiceJob;
    private final Job invoiceProcessingJob;
    private final Job weeklyInvoiceStatisticsJob;
    private final Job monthlyInvoiceStatisticsJob;

//
//    // 0, 30초마다 실행
//    @Scheduled(cron = "0,30 * * * * ?")
//    public void generateInvoice() {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("invoice", System.currentTimeMillis())
//                .toJobParameters();
//        try {
//            log.info("JPA 배치 프로그램 실행 시작");
//            jobLauncher.run(generateInvoiceJob, jobParameters);
//            log.info("JPA 배치 프로그램 실행 완료");
//        } catch (JobExecutionAlreadyRunningException e) {
//            log.error("JobExecutionAlreadyRunningException 발생: ", e);
//        } catch (JobRestartException e) {
//            log.error("JobRestartException 발생: ", e);
//        } catch (JobInstanceAlreadyCompleteException e) {
//            log.error("JobInstanceAlreadyCompleteException 발생: ", e);
//        } catch (JobParametersInvalidException e) {
//            log.error("JobParametersInvalidException 발생: ", e);
//        } catch (Exception e) {
//            log.error("예기치 않은 오류 발생: ", e);
//        }
//    }
//
//    // 15, 45초마다 실행
//    @Scheduled(cron = "15,45 * * * * ?")
//    public void jdbcGenerateInvoice() {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("jdbcInvoice", System.currentTimeMillis())
//                .toJobParameters();
//        try {
//            log.info("JDBC 배치 프로그램 실행 시작");
//            jobLauncher.run(jdbcGenerateInvoiceJob, jobParameters);
//            log.info("JDBC 배치 프로그램 실행 완료");
//        } catch (JobExecutionAlreadyRunningException e) {
//            log.error("JobExecutionAlreadyRunningException 발생: ", e);
//        } catch (JobRestartException e) {
//            log.error("JobRestartException 발생: ", e);
//        } catch (JobInstanceAlreadyCompleteException e) {
//            log.error("JobInstanceAlreadyCompleteException 발생: ", e);
//        } catch (JobParametersInvalidException e) {
//            log.error("JobParametersInvalidException 발생: ", e);
//        } catch (Exception e) {
//            log.error("예기치 않은 오류 발생: ", e);
//        }
//    }
//
//  15, 45초마다 실행monthlyInvoiceStatisticsJob
    @Scheduled(cron = "15,45 * * * * ?")
    public void runInvoiceProcessingJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("InvoiceProcessingJob", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("Invoice Processing Job 실행 시작");
            jobLauncher.run(invoiceProcessingJob, jobParameters);
            log.info("Invoice Processing Job 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            log.error("JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            log.error("JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: ", e);
        }
    }

//
//    //        @Scheduled(cron = "0 0 1 * * SUN") // 매주 일요일 새벽 1시에 실행
//    @Scheduled(cron = "15,45 * * * * ?")
//    public void weeklyJob() {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("weeklyInvoiceStatisticsJob", System.currentTimeMillis())
//                .toJobParameters();
//        try {
//            log.info("weeklyInvoiceStatisticsJob 실행 시작");
//            jobLauncher.run(weeklyInvoiceStatisticsJob, jobParameters);
//            log.info("weeklyInvoiceStatisticsJob 실행 완료");
//        } catch (JobExecutionAlreadyRunningException e) {
//            log.error("JobExecutionAlreadyRunningException 발생: ", e);
//        } catch (JobRestartException e) {
//            log.error("JobRestartException 발생: ", e);
//        } catch (JobInstanceAlreadyCompleteException e) {
//            log.error("JobInstanceAlreadyCompleteException 발생: ", e);
//        } catch (JobParametersInvalidException e) {
//            log.error("JobParametersInvalidException 발생: ", e);
//        } catch (Exception e) {
//            log.error("예기치 않은 오류 발생: ", e);
//        }
//    }
//
////        @Scheduled(cron = "0 0 1 1 * ?") // 매월 1일 새벽 1시
//        @Scheduled(cron = "0,30 * * * * ?")
//        public void monthlyJob() {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("monthlyInvoiceStatisticsJob", System.currentTimeMillis())
//                    .toJobParameters();
//            try {
//                log.info("monthlyInvoiceStatisticsJob 실행 시작");
//                jobLauncher.run(monthlyInvoiceStatisticsJob, jobParameters);
//                log.info("monthlyInvoiceStatisticsJob 실행 완료");
//            } catch (JobExecutionAlreadyRunningException e) {
//                log.error("JobExecutionAlreadyRunningException 발생: ", e);
//            } catch (JobRestartException e) {
//                log.error("JobRestartException 발생: ", e);
//            } catch (JobInstanceAlreadyCompleteException e) {
//                log.error("JobInstanceAlreadyCompleteException 발생: ", e);
//            } catch (JobParametersInvalidException e) {
//                log.error("JobParametersInvalidException 발생: ", e);
//            } catch (Exception e) {
//                log.error("예기치 않은 오류 발생: ", e);
//            }

//        }
    }




