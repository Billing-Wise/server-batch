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
import org.springframework.transaction.PlatformTransactionManager;
import site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.rowmapper.StaticsInvoiceRowMapper;
import site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer.CustomMonthlyInvoiceWriter;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.batch.listner.statistic.MonthlyInvoiceStatisticsListener;
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

    @Bean
    public Job monthlyInvoiceStatisticsJob(JobRepository jobRepository, Step monthlyInvoiceStatisticsStep) {
        return new JobBuilder("monthlyInvoiceStatisticsJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(monthlyInvoiceStatisticsStep)
                .build();
    }

    @Bean
    Step monthlyInvoiceStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("monthlyInvoiceStatisticsStep", jobRepository)
                .<Invoice, Invoice>chunk(CHUNK_SIZE, transactionManager)
                .reader(monthlyInvoiceReader())
                .writer(monthlyInvoiceWriter())
                .listener(monthlyInvoiceStatisticsListener)
                .build();
    }

    private ItemWriter<? super Invoice> monthlyInvoiceWriter() {
        return new CustomMonthlyInvoiceWriter(monthlyInvoiceStatisticsListener);
    }

    private ItemReader<? extends Invoice> monthlyInvoiceReader() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1).withDayOfMonth(1);
        LocalDateTime endDate = startDate.plusMonths(1).minusDays(1);
        return new JdbcCursorItemReaderBuilder<Invoice>()
                .name("monthlyInvoiceReader")
                .dataSource(dataSource)
                .fetchSize(CHUNK_SIZE)
                .sql("SELECT i.*, c.member_id, m.client_id FROM invoice i " +
                        "JOIN contract c ON i.contract_id = c.contract_id  " +
                        "JOIN member m ON c.member_id = m.member_id " +
                        "WHERE i.due_date >= ? AND i.due_date <= ?")
                .preparedStatementSetter(new ArgumentPreparedStatementSetter(new Object[]{startDate, endDate}))
                .rowMapper(new StaticsInvoiceRowMapper())
                .build();
    }
}

