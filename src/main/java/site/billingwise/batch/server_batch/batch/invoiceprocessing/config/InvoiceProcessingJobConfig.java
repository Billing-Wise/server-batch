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
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.batch.service.EmailService;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

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
        return new StepBuilder("InvoiceSendingAndPaymentManageStep", jobRepository)
                .<Invoice, Invoice>chunk(CHUNK_SIZE, transactionManager)
                .reader(invoiceSendingAndPaymentManageReader())
                .writer(invoiceSendingAndPaymentManageWriter())
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

        return new JdbcCursorItemReaderBuilder<Invoice>()
                .name("invoiceSendingAndPaymentManageReader")
                .fetchSize(CHUNK_SIZE)
                .sql("select i.*, c.member_id, m.email, m.name, m.phone, ca.number, ca.bank, ca.owner, i.is_deleted, c.is_subscription " +
                        "from invoice i " +
                        "join contract c ON i.contract_id = c.contract_id " +
                        "join member m ON c.member_id = m.member_id " +
                        "left join consent_account ca ON m.member_id = ca.member_id " +
                        "where i.contract_date >= curdate() AND i.contract_date < curdate() + interval 1 day")
                .rowMapper(new InvoiceRowMapper())
                .dataSource(dataSource)
                .build();
    }

    private ItemWriter<? super Invoice> invoiceSendingAndPaymentManageWriter() {
        return new InvoiceSendingAndPaymentManageWriter(jdbcTemplate, emailService);
    }
}
