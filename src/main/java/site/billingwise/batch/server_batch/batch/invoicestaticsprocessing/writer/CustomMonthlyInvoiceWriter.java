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
            // 클라이언트별 통계 객체 생성 또는 가져오기
            Long invoiceClientId = invoice.getContract().getMember().getClient().getId();
            MonthlyInvoiceStatisticsListener clientStatistics = clientStatisticsMap.computeIfAbsent(invoiceClientId, k -> new MonthlyInvoiceStatisticsListener(jdbcTemplate));
            clientStatistics.setClientId(invoiceClientId);


            // 대기 상태의 청구서는 처리하지 않음
            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_PENDING) {
                continue;
            }

            // 총 청구 금액 추가
            clientStatistics.addInvoice(invoice.getChargeAmount());

            // 결제 상태에 따라 수금액 또는 미수금액 추가
            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_COMPLETED) {
                clientStatistics.addCollected(invoice.getChargeAmount());
            } else if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_UNPAID) {
                clientStatistics.addOutstanding(invoice.getChargeAmount());
            }
        }
    }

    // 통계 저장 및 초기화
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
        clientStatisticsMap.clear();
        invoiceStatisticsListener.resetStatistics();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        saveAndResetStatistics();
        return ExitStatus.COMPLETED;
    }
}