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

//    0, 30초마다 실행
//    청구 생성 (jpa)
//    jdbcGenerateInvoice랑 같은 기능
//    @Scheduled(cron = "0,30 * * * * ?")
//    public void generateInvoice() {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("JPAinvoice", System.currentTimeMillis())
//                .toJobParameters();
//        try {
//            log.info("Scheduler generateInvoice - JPA 배치 프로그램 실행 시작");
//            jobLauncher.run(generateInvoiceJob, jobParameters);
//            log.info("Scheduler generateInvoice - JPA 배치 프로그램 실행 완료");
//        } catch (JobExecutionAlreadyRunningException e) {
//            log.error("Scheduler generateInvoice JobExecutionAlreadyRunningException 발생: ", e);
//        } catch (JobRestartException e) {
//            log.error("Scheduler generateInvoice JobRestartException 발생: ", e);
//        } catch (JobInstanceAlreadyCompleteException e) {
//            log.error("Scheduler generateInvoice JobInstanceAlreadyCompleteException 발생: ", e);
//        } catch (JobParametersInvalidException e) {
//            log.error("Scheduler generateInvoice JobParametersInvalidException 발생: ", e);
//        } catch (Exception e) {
//            log.error("Scheduler generateInvoice 예기치 않은 오류 발생: ", e);
//        }
//    }

//  청구 생성 잡
//  15, 45초마다 실행( 테스트 용 )
//  @Scheduled(cron = "15,45 * * * * ?")
//  매달 말일 새벽 3시에 작동
     @Scheduled(cron = "0 0 3 L * ?")
     public void jdbcGenerateInvoice() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("jdbcInvoice", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("Scheduler jdbcGenerateInvoice - JDBC 배치 프로그램 실행 시작");
            jobLauncher.run(jdbcGenerateInvoiceJob, jobParameters);
            log.info("Scheduler jdbcGenerateInvoice - JDBC 배치 프로그램 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Scheduler jdbcGenerateInvoice JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            log.error("Scheduler jdbcGenerateInvoice JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Scheduler jdbcGenerateInvoice JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            log.error("Scheduler jdbcGenerateInvoice JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            log.error("Scheduler jdbcGenerateInvoice 예기치 않은 오류 발생: ", e);
        }
    }

    // 결제 처리 및 결제 기한(Due_date) 지난 납부자 결제 처리 잡
    // 15, 45초마다 실행( 테스트용 )
    // @Scheduled(cron = "15,45 * * * * ?")
    // 매일 새벽 1시에 작동
    @Scheduled(cron = "0 0 1 * * ?")
    public void runInvoiceProcessingJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("InvoiceProcessingJob", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("Scheduler runInvoiceProcessingJob - Invoice Processing Job 실행 시작");
            jobLauncher.run(invoiceProcessingJob, jobParameters);
            log.info("Scheduler runInvoiceProcessingJob - Invoice Processing Job 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Scheduler runInvoiceProcessingJob JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            log.error("Scheduler runInvoiceProcessingJob JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Scheduler runInvoiceProcessingJob JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            log.error("Scheduler runInvoiceProcessingJob JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            log.error("Scheduler runInvoiceProcessingJob 예기치 않은 오류 발생: ", e);
        }
    }

    // 주간 청구액 및 수납액 집계 통계 처리 잡
    // 15, 45초마다 실행( 테스트용 )
    // @Scheduled(cron = "15,45 * * * * ?")
    // 매주 월요일 새벽 5시에 작동
    @Scheduled(cron = "0 0 5 ? * MON")
    public void weeklyJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("weeklyInvoiceStatisticsJob", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("Scheduler weeklyJob - weeklyInvoiceStatisticsJob 실행 시작");
            jobLauncher.run(weeklyInvoiceStatisticsJob, jobParameters);
            log.info("Scheduler weeklyJob - weeklyInvoiceStatisticsJob 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Scheduler weeklyJob JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            log.error("Scheduler weeklyJob JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Scheduler weeklyJob JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            log.error("Scheduler weeklyJob JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            log.error("Scheduler weeklyJob 예기치 않은 오류 발생: ", e);
        }
    }

    // 월간 청구액 및 수납액 집계 통계 처리 잡
    // 0, 30초마다 실행( 테스트용 )
    // @Scheduled(cron = "0,30 * * * * ?")
    // 매월 1일 새벽 6시에 작동
    @Scheduled(cron = "0 0 6 1 * ?")
    public void monthlyJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("monthlyInvoiceStatisticsJob", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("Scheduler monthlyJob - monthlyInvoiceStatisticsJob 실행 시작");
            jobLauncher.run(monthlyInvoiceStatisticsJob, jobParameters);
            log.info("Scheduler monthlyJob - monthlyInvoiceStatisticsJob 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Scheduler monthlyJob JobExecutionAlreadyRunningException 발생: ", e);
        } catch (JobRestartException e) {
            log.error("Scheduler monthlyJob JobRestartException 발생: ", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Scheduler monthlyJob JobInstanceAlreadyCompleteException 발생: ", e);
        } catch (JobParametersInvalidException e) {
            log.error("Scheduler monthlyJob JobParametersInvalidException 발생: ", e);
        } catch (Exception e) {
            log.error("Scheduler monthlyJob 예기치 않은 오류 발생: ", e);
        }
    }
}
