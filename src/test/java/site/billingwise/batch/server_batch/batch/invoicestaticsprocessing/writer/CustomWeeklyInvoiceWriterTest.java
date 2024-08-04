package site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer;

import static org.mockito.Mockito.*;
import static site.billingwise.batch.server_batch.batch.util.StatusConstants.PAYMENT_STATUS_PENDING;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;

import site.billingwise.batch.server_batch.batch.listner.statistic.WeeklyInvoiceStatisticsListener;

import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;
import site.billingwise.batch.server_batch.domain.member.Member;
import site.billingwise.batch.server_batch.domain.user.Client;

public class CustomWeeklyInvoiceWriterTest {

    @Mock
    private WeeklyInvoiceStatisticsListener invoiceStatisticsListener;

    @InjectMocks
    private CustomWeeklyInvoiceWriter writer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new CustomWeeklyInvoiceWriter(invoiceStatisticsListener);
    }

    @Test
    public void testWrite() throws Exception {
        Client client = Client.builder().id(1L).build();
        Member member = Member.builder().client(client).build();
        Contract contract = Contract.builder().member(member).build();
        PaymentStatus paymentStatus = PaymentStatus.builder().id(2L).build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .contract(contract)
                .chargeAmount(1000L)
                .paymentStatus(paymentStatus)
                .isDeleted(false)
                .build();

        when(invoiceStatisticsListener.getClientId()).thenReturn(null);

        Chunk<Invoice> chunk = new Chunk<>(List.of(invoice));

        writer.write(chunk);

        verify(invoiceStatisticsListener, times(1)).setClientId(client.getId());
        verify(invoiceStatisticsListener, times(1)).addInvoice(invoice.getChargeAmount());
        verify(invoiceStatisticsListener, times(1)).addCollected(invoice.getChargeAmount());
    }

    @Test
    public void testWriteWithPendingInvoice() throws Exception {

        Client client = Client.builder().id(1L).build();
        Member member = Member.builder().client(client).build();
        Contract contract = Contract.builder().member(member).build();
        PaymentStatus paymentStatus = PaymentStatus.builder().id(PAYMENT_STATUS_PENDING).build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .contract(contract)
                .chargeAmount(1000L)
                .paymentStatus(paymentStatus)
                .isDeleted(false)
                .build();

        when(invoiceStatisticsListener.getClientId()).thenReturn(null);

        Chunk<Invoice> chunk = new Chunk<>(List.of(invoice));


        writer.write(chunk);


        verify(invoiceStatisticsListener, times(1)).setClientId(client.getId());
        verify(invoiceStatisticsListener, never()).addInvoice(anyLong());
        verify(invoiceStatisticsListener, never()).addCollected(anyLong());
        verify(invoiceStatisticsListener, never()).addOutstanding(anyLong());
    }

}