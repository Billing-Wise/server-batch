package site.billingwise.batch.server_batch.batch.invoiceprocessing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import site.billingwise.batch.server_batch.batch.invoiceprocessing.rowmapper.InvoiceRowMapper;
import site.billingwise.batch.server_batch.batch.invoiceprocessing.tasklet.CustomUpdateOverdueInvoicesTasklet;
import site.billingwise.batch.server_batch.batch.invoiceprocessing.writer.InvoiceSendingAndPaymentManageWriter;
import site.billingwise.batch.server_batch.batch.listner.CustomRetryListener;
import site.billingwise.batch.server_batch.batch.listner.CustomSkipListener;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.batch.policy.backoff.CustomBackOffPolicy;
import site.billingwise.batch.server_batch.batch.policy.skip.CustomSkipPolicy;
import site.billingwise.batch.server_batch.batch.service.EmailService;
import site.billingwise.batch.server_batch.batch.service.SmsService;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.feign.PayClient;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class InvoiceProcessingJobConfig {

    private final int CHUNK_SIZE = 100;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final JobCompletionCheckListener jobCompletionCheckListener;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PayClient payClient;
    private final CustomRetryListener retryListener;
    private final CustomSkipListener customSkipListener;
    private final CustomSkipPolicy customSkipPolicy;

    @Bean
    public Job invoiceProcessingJob(JobRepository jobRepository, Step invoiceSendingAndPaymentManageStep, Step invoiceDueDateUpdateStep) {
        return new JobBuilder("InvoiceProcessingJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(invoiceSendingAndPaymentManageStep)
                .next(invoiceDueDateUpdateStep)
                .build();
    }

    @Bean
    public Step invoiceSendingAndPaymentManageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        //backoff 정책 만들기
        CustomBackOffPolicy customBackOffPolicy = new CustomBackOffPolicy(1000L, 2.0, 10000L);

        return new StepBuilder("InvoiceSendingAndPaymentManageStep", jobRepository)
                .<Invoice, Invoice>chunk(CHUNK_SIZE, transactionManager)
                .reader(invoiceSendingAndPaymentManageReader())
                .writer(invoiceSendingAndPaymentManageWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(5)
                .backOffPolicy(customBackOffPolicy)
                .listener(retryListener)
                .skip(Exception.class)
                .skipPolicy(customSkipPolicy)
                .listener(customSkipListener)
                .build();

    }

    @Bean
    public Step invoiceDueDateUpdateStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("invoiceDueDateUpdateStep", jobRepository)
                .tasklet(updateOverdueInvoicesTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet updateOverdueInvoicesTasklet() {
        return new CustomUpdateOverdueInvoicesTasklet(jdbcTemplate);
    }


    private ItemReader<? extends Invoice> invoiceSendingAndPaymentManageReader() {

        String sql = """
                    select 
                        inv.invoice_id, inv.contract_id, inv.payment_type_id, inv.payment_status_id, 
                        inv.charge_amount, inv.contract_date, inv.due_date, inv.is_deleted, 
                        con.member_id, mem.email, mem.phone,  mem.name, consent_acc.number, consent_acc.bank, consent_acc.owner, con.is_subscription 
                    from invoice inv 
                    join contract con ON inv.contract_id = con.contract_id 
                    join member mem ON con.member_id = mem.member_id 
                    left join consent_account consent_acc ON mem.member_id = consent_acc.member_id 
                    where inv.contract_date >= curdate() AND inv.contract_date < curdate() + interval 1 day 
                    and inv.is_deleted = false
                """;

        return new JdbcCursorItemReaderBuilder<Invoice>()
                .name("invoiceSendingAndPaymentManageReader")
                .fetchSize(CHUNK_SIZE)
                .sql(sql)
                .rowMapper(new InvoiceRowMapper())
                .dataSource(dataSource)
                .build();
    }

    private ItemWriter<? super Invoice> invoiceSendingAndPaymentManageWriter() {
        return new InvoiceSendingAndPaymentManageWriter(jdbcTemplate, emailService, smsService, payClient);
    }
}