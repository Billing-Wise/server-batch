package site.billingwise.batch.server_batch.batch.generateinvoice;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import site.billingwise.batch.server_batch.batch.listner.JobCompletionCheckListener;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.repository.InvoiceRepository;
import site.billingwise.batch.server_batch.domain.invoice.repository.PaymentStatusRepository;

@Configuration
@RequiredArgsConstructor
public class GenerateInvoiceJobConfig {

    private final int CHUNK_SIZE = 100;
    private final EntityManagerFactory entityManagerFactory;
    private final JobCompletionCheckListener jobCompletionCheckListener;

    @Bean
    public Job generateInvoiceJob(JobRepository jobRepository, Step generateInvoiceStep) {
        return new JobBuilder("generateInvoiceJob", jobRepository)
                .listener(jobCompletionCheckListener)
                .start(generateInvoiceStep)
                .build();
    }

    @Bean
    public Step generateInvoiceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                    JpaPagingItemReader<Contract> contractItemReader, ItemWriter<Contract> contractItemWriter) {
        return new StepBuilder("generateInvoiceStep", jobRepository)
                .<Contract, Contract>chunk(CHUNK_SIZE, transactionManager)
                .reader(contractItemReader)
                .writer(contractItemWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Contract> contractItemReader() {
        JpaPagingItemReader<Contract> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT c FROM Contract c WHERE c.contractStatus.name = '진행' ORDER BY c.id ASC");
        reader.setPageSize(CHUNK_SIZE);
        return reader;
    }

    @Bean
    public ItemWriter<Contract> contractItemWriter(PaymentStatusRepository paymentStatusRepository, InvoiceRepository invoiceRepository) {
        return new GenerateInvoiceWriter(paymentStatusRepository, invoiceRepository);
    }
}