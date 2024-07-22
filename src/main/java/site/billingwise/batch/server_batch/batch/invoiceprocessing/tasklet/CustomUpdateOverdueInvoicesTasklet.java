package site.billingwise.batch.server_batch.batch.invoiceprocessing.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.PAYMENT_STATUS_PENDING;
import static site.billingwise.batch.server_batch.batch.util.StatusConstants.PAYMENT_STATUS_UNPAID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUpdateOverdueInvoicesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String sql = "update invoice set payment_status_id = ? where due_date < curdate() and payment_status_id = ? and is_deleted = false";
        int updateRows = jdbcTemplate.update(sql, PAYMENT_STATUS_UNPAID, PAYMENT_STATUS_PENDING);

        log.info("납부자 결제 대기에서 미납으로 전환된 청구 데이터 수: {}", updateRows);

        return RepeatStatus.FINISHED;
    }
}
