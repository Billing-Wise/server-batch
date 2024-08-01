package site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.batch.listner.statistic.MonthlyInvoiceStatisticsListener;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

import java.util.HashMap;
import java.util.Map;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.*;


@Component
@RequiredArgsConstructor
@Slf4j
public class CustomMonthlyInvoiceWriter implements ItemWriter<Invoice>, StepExecutionListener {

    private final MonthlyInvoiceStatisticsListener invoiceStatisticsListener;
    private final JdbcTemplate jdbcTemplate;
    private final Map<Long, MonthlyInvoiceStatisticsListener> clientStatisticsMap = new HashMap<>();

    @Override
    public void write(Chunk<? extends Invoice> chunk) {
        for (Invoice invoice : chunk) {
            Long invoiceClientId = invoice.getContract().getMember().getClient().getId();
            MonthlyInvoiceStatisticsListener clientStatistics = clientStatisticsMap.computeIfAbsent(invoiceClientId, k -> new MonthlyInvoiceStatisticsListener(jdbcTemplate));
            clientStatistics.setClientId(invoiceClientId);

            log.info("월간 통계 invoice 데이터 ID: {}", invoice.getId());

            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_PENDING) {
                log.info("아직 결제 대기 중인 invoice 데이터, skipping: {}", invoice.getId());
                continue;
            }

            clientStatistics.addInvoice(invoice.getChargeAmount());
            log.info("총 청구액에 더할 invoice 데이터 금액: {}", invoice.getChargeAmount());

            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_COMPLETED) {
                clientStatistics.addCollected(invoice.getChargeAmount());
                log.info("총 수납금액에 더할 invoice 데이터 금액: {}", invoice.getChargeAmount());
            } else if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_UNPAID) {
                clientStatistics.addOutstanding(invoice.getChargeAmount());
                log.info("총 미납금액에 더할 invoice 데이터 금액: {}", invoice.getChargeAmount());
            }
        }
    }

    private void saveAndResetStatistics() {
        clientStatisticsMap.forEach((clientId, statistics) -> {
            if (statistics.getTotalInvoicedMoney() > 0) {
                statistics.saveStatistics();
            }
            statistics.resetStatistics();
        });
        clientStatisticsMap.clear();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("beforeStep 호출");
        clientStatisticsMap.clear();
        invoiceStatisticsListener.resetStatistics();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("afterStep 호출");
        saveAndResetStatistics();
        return ExitStatus.COMPLETED;
    }
}