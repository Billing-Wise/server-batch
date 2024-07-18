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
import site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer.CustomWeeklyInvoiceWriter;
import site.billingwise.batch.server_batch.batch.listner.InvoiceStatisticsListener;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

import javax.sql.DataSource;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeeklyInvoiceStaticsJobConfig {

    private final int CHUNK_SIZE = 100;
    private final DataSource dataSource;
    private final JobCompletionCheckListener jobCompletionCheckListener;
    private final InvoiceStatisticsListener invoiceStatisticsListener;


    @Bean
    public Job weeklyInvoiceStatisticsJob(JobRepository jobRepository, Step weeklyInvoiceStatisticsStep) {
        return new JobBuilder("weeklyInvoiceStatisticsJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(weeklyInvoiceStatisticsStep)
                .build();
    }

    @Bean
    Step weeklyInvoiceStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new StepBuilder("weeklyInvoiceStatisticsStep", jobRepository)
                .<Invoice, Invoice>chunk(CHUNK_SIZE, transactionManager)
                .reader(weeklyInvoiceReader())
                .writer(weeklyInvoiceWriter())
                .listener(invoiceStatisticsListener)
                .build();

    }


    private ItemWriter<? super Invoice> weeklyInvoiceWriter() {
        return new CustomWeeklyInvoiceWriter(invoiceStatisticsListener);
    }

    private ItemReader<? extends Invoice> weeklyInvoiceReader() {
        LocalDateTime startDate = LocalDateTime.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDateTime endDate = startDate.plusDays(6);
        return new JdbcCursorItemReaderBuilder<Invoice>()
                .name("weeklyInvoiceReader")
                .dataSource(dataSource)
                .fetchSize(CHUNK_SIZE)
                .sql("SELECT i.*, c.member_id, m.client_id FROM invoice i " +
                        "JOIN contract c ON i.contract_id = c.contract_id  " +
                        "JOIN member m ON c.member_id = m.member_id " +
                        "WHERE i.due_date >= ? AND i.due_date <= ?")           .preparedStatementSetter(new ArgumentPreparedStatementSetter(new Object[]{startDate, endDate}))
                .rowMapper(new StaticsInvoiceRowMapper())
                .build();
    }
}
