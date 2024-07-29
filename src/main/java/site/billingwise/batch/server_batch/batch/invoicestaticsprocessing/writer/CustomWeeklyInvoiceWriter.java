package site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.batch.listner.statistic.WeeklyInvoiceStatisticsListener;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomWeeklyInvoiceWriter implements ItemWriter<Invoice> {

    private final WeeklyInvoiceStatisticsListener invoiceStatisticsListener;

    @Override
    public void write(Chunk<? extends Invoice> chunk)  {
        for (Invoice invoice : chunk) {


            if (invoiceStatisticsListener.getClientId() == null) {
                invoiceStatisticsListener.setClientId(invoice.getContract().getMember().getClient().getId());
            }


            log.info("주간 통계 invoice 데이터 ID: {}", invoice.getId());


            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_PENDING) {
                log.info("아직 결제 대기 중인 invoice 데이터, skipping: {}", invoice.getId());
                continue;
            }

            invoiceStatisticsListener.addInvoice(invoice.getChargeAmount());
            log.info("총 청구액에 더할 invoice 데이터 금액: {}", invoice.getChargeAmount());

            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_COMPLETED) {
                invoiceStatisticsListener.addCollected(invoice.getChargeAmount());
                log.info("총 수납금액에 더할 invoice 데이터 금액: {}", invoice.getChargeAmount());
            } else if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_UNPAID) {
                invoiceStatisticsListener.addOutstanding(invoice.getChargeAmount());
                log.info("총 미납금액에 더할 invoice 데이터 금액: {}", invoice.getChargeAmount());
            }
        }
    }
}