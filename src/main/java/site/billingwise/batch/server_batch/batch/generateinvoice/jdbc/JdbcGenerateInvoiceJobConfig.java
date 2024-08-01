package site.billingwise.batch.server_batch.batch.generateinvoice.jdbc;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.policy.MapRetryContextCache;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import site.billingwise.batch.server_batch.batch.listner.CustomRetryListener;
import site.billingwise.batch.server_batch.batch.listner.CustomSkipListener;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.batch.generateinvoice.rowmapper.JdbcContractRowMapper;
import site.billingwise.batch.server_batch.batch.policy.backoff.CustomBackOffPolicy;
import site.billingwise.batch.server_batch.batch.policy.retry.CustomRetryPolicy;
import site.billingwise.batch.server_batch.batch.policy.skip.CustomSkipPolicy;
import site.billingwise.batch.server_batch.domain.contract.Contract;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class JdbcGenerateInvoiceJobConfig {

    private final int CHUNK_SIZE = 100;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final JobCompletionCheckListener jobCompletionCheckListener;
    private final CustomRetryListener retryListener;
    private final CustomSkipListener customSkipListener;
    private final CustomSkipPolicy customSkipPolicy;

    @Bean
    public Job jdbcGenerateInvoiceJob(JobRepository jobRepository, Step jdbcGenerateInvoiceStep) {
        return new JobBuilder("jdbcGenerateInvoiceJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(jdbcGenerateInvoiceStep)
                .build();
    }



    @Bean
    public Step jdbcGenerateInvoiceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        CustomBackOffPolicy customBackOffPolicy = new CustomBackOffPolicy(1000L, 2.0, 10000L);

        TaskletStep jdbcGenerateInvoiceStep = new StepBuilder("jdbcGenerateInvoiceStep", jobRepository)
                .<Contract, Contract>chunk(CHUNK_SIZE, transactionManager)
                .reader(jdbcContractItemReader())
                .writer(jdbcContractItemWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(5)
                .backOffPolicy(customBackOffPolicy)
                .listener(retryListener)
                .skip(Exception.class)
                .skipPolicy(customSkipPolicy)
                .listener(customSkipListener)
                .build();
        return jdbcGenerateInvoiceStep;
    }

    @Bean
    public ItemReader<Contract> jdbcContractItemReader() {
        String sql = """
            select con.contract_id, con.invoice_type_id, con.payment_type_id, con.contract_cycle, 
            con.item_price, con.item_amount, con.is_deleted, con.is_subscription 
            from contract con 
            where con.contract_status_id = 2 and con.is_deleted = false
        """;

        return new JdbcCursorItemReaderBuilder<Contract>()
                .name("jdbcContractItemReader")
                .fetchSize(CHUNK_SIZE)
                .sql(sql)
                .rowMapper(new JdbcContractRowMapper())
                .dataSource(dataSource)
                .build();
    }




    @Bean
    public ItemWriter<Contract> jdbcContractItemWriter() {
        return new JdbcGenerateInvoiceWriter(jdbcTemplate);
    }
}
