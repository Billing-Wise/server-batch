package site.billingwise.batch.server_batch.batch.generateinvoice.jdbc;


import lombok.RequiredArgsConstructor;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.batch.generateinvoice.rowmapper.JdbcContractRowMapper;
import site.billingwise.batch.server_batch.domain.contract.Contract;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcGenerateInvoiceJobConfig {

    private final int CHUNK_SIZE = 100;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final JobCompletionCheckListener jobCompletionCheckListener;

    @Bean
    public Job jdbcGenerateInvoiceJob(JobRepository jobRepository, Step jdbcGenerateInvoiceStep) {
        return new JobBuilder("jdbcGenerateInvoiceJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(jdbcGenerateInvoiceStep)
                .build();
    }


    @Bean
    public Step jdbcGenerateInvoiceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jdbcGenerateInvoiceStep", jobRepository)
                .<Contract, Contract>chunk(CHUNK_SIZE, transactionManager)
                .reader(jdbcContractItemReader())
                .writer(jdbcContractItemWriter())
                .build();
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
