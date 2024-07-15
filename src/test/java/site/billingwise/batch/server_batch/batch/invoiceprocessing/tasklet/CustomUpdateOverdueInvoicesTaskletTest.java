package site.billingwise.batch.server_batch.batch.invoiceprocessing.tasklet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUpdateOverdueInvoicesTaskletTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CustomUpdateOverdueInvoicesTasklet tasklet;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    private static final long PAYMENT_STATUS_UNPAID = 1L;
    private static final long PAYMENT_STATUS_PENDING = 3L;

    @BeforeEach
    public void setUp() {
        tasklet = new CustomUpdateOverdueInvoicesTasklet(jdbcTemplate);
    }

    @Test
    @DisplayName("납부자 결제 미납 전환")
    public void testExecute() throws Exception {

        when(jdbcTemplate.update("update invoice set payment_status_id = ? where due_date < curdate() and payment_status_id = ? and is_deleted = false", PAYMENT_STATUS_UNPAID, PAYMENT_STATUS_PENDING))
                .thenReturn(5);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        verify(jdbcTemplate).update("update invoice set payment_status_id = ? where due_date < curdate() and payment_status_id = ? and is_deleted = false", PAYMENT_STATUS_UNPAID, PAYMENT_STATUS_PENDING);

        assertEquals(RepeatStatus.FINISHED, status);

    }
}