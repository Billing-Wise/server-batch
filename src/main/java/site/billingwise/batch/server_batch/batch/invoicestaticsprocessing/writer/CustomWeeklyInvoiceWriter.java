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
    private Long currentClientId = null;

    @Override
    public void write(Chunk<? extends Invoice> chunk) {
        for (Invoice invoice : chunk) {
            Long invoiceClientId = invoice.getContract().getMember().getClient().getId();


            if (currentClientId == null) {
                currentClientId = invoiceClientId;
                invoiceStatisticsListener.setClientId(currentClientId);
            } else if (!currentClientId.equals(invoiceClientId)) {
                // 클라이언트가 변경되면 기존 집계 데이터를 저장하고 초기화 하기
                if (invoiceStatisticsListener.getTotalInvoicedMoney() > 0) {
                    invoiceStatisticsListener.saveStatistics();
                    invoiceStatisticsListener.resetStatistics();
                }
                currentClientId = invoiceClientId;
                invoiceStatisticsListener.setClientId(currentClientId);
            }


            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_PENDING) {
                continue;
            }

            invoiceStatisticsListener.addInvoice(invoice.getChargeAmount());

            if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_COMPLETED) {
                invoiceStatisticsListener.addCollected(invoice.getChargeAmount());
            } else if (invoice.getPaymentStatus().getId() == PAYMENT_STATUS_UNPAID) {
                invoiceStatisticsListener.addOutstanding(invoice.getChargeAmount());
            }
        }

        // 마지막 클라이언트의 집계 데이터를 처리
        if (currentClientId != null && invoiceStatisticsListener.getTotalInvoicedMoney() > 0) {
            invoiceStatisticsListener.saveStatistics();
            currentClientId = null;
        }
    }
}