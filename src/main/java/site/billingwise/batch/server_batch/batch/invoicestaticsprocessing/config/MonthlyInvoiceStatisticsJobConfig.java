package site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.rowmapper.StaticsInvoiceRowMapper;
import site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer.CustomMonthlyInvoiceWriter;
import site.billingwise.batch.server_batch.batch.listner.CustomRetryListener;
import site.billingwise.batch.server_batch.batch.listner.CustomSkipListener;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.batch.listner.statistic.MonthlyInvoiceStatisticsListener;
import site.billingwise.batch.server_batch.batch.policy.backoff.CustomBackOffPolicy;
import site.billingwise.batch.server_batch.batch.policy.skip.CustomSkipPolicy;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MonthlyInvoiceStatisticsJobConfig {

    private final int CHUNK_SIZE = 100;
    private final DataSource dataSource;
    private final JobCompletionCheckListener jobCompletionCheckListener;
    private final MonthlyInvoiceStatisticsListener monthlyInvoiceStatisticsListener;
    private final JdbcTemplate jdbcTemplate;
    private final CustomRetryListener retryListener;
    private final CustomSkipListener customSkipListener;
    private final CustomSkipPolicy customSkipPolicy;


    @Bean
    public Job monthlyInvoiceStatisticsJob(JobRepository jobRepository, Step monthlyInvoiceStatisticsStep) {
        return new JobBuilder("monthlyInvoiceStatisticsJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(monthlyInvoiceStatisticsStep)
                .build();
    }

    @Bean
    Step monthlyInvoiceStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        CustomBackOffPolicy customBackOffPolicy = new CustomBackOffPolicy(1000L, 2.0, 10000L);

        return new StepBuilder("monthlyInvoiceStatisticsStep", jobRepository)
                .<Invoice, Invoice>chunk(CHUNK_SIZE, transactionManager)
                .reader(monthlyInvoiceReader())
                .writer(monthlyInvoiceWriter())
                .listener(monthlyInvoiceStatisticsListener)
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

    private ItemWriter<? super Invoice> monthlyInvoiceWriter() {
        return new CustomMonthlyInvoiceWriter(monthlyInvoiceStatisticsListener, jdbcTemplate);
    }

    private ItemReader<? extends Invoice> monthlyInvoiceReader() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1).withDayOfMonth(1);
        LocalDateTime endDate = startDate.plusMonths(1).minusDays(1);

        String sql = """
        select inv.invoice_id, inv.charge_amount, inv.due_date, inv.is_deleted, 
               inv.payment_status_id, con.contract_id, con.member_id, 
               mem.client_id
        from invoice inv
        join contract con ON inv.contract_id = con.contract_id
        join member mem ON con.member_id = mem.member_id
        where inv.due_date >= ? AND inv.due_date <= ? AND inv.is_deleted = false
        """;

        return new JdbcCursorItemReaderBuilder<Invoice>()
                .name("monthlyInvoiceReader")
                .dataSource(dataSource)
                .fetchSize(CHUNK_SIZE)
                .sql(sql)
                .preparedStatementSetter(new ArgumentPreparedStatementSetter(new Object[]{startDate, endDate}))
                .rowMapper(new StaticsInvoiceRowMapper())
                .build();
    }
}

