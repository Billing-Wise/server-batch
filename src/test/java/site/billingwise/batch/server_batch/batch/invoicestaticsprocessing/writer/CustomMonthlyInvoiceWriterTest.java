package site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.writer;

import static org.mockito.Mockito.*;
import static site.billingwise.batch.server_batch.batch.util.StatusConstants.PAYMENT_STATUS_PENDING;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;

import org.springframework.jdbc.core.JdbcTemplate;
import site.billingwise.batch.server_batch.batch.listner.statistic.MonthlyInvoiceStatisticsListener;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;
import site.billingwise.batch.server_batch.domain.member.Member;
import site.billingwise.batch.server_batch.domain.user.Client;



public class CustomMonthlyInvoiceWriterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CustomMonthlyInvoiceWriter writer;

    private static final long PAYMENT_STATUS_PENDING = 3L;
    private static final long PAYMENT_STATUS_COMPLETED = 2L;
    private static final long PAYMENT_STATUS_UNPAID = 4L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new CustomMonthlyInvoiceWriter(new MonthlyInvoiceStatisticsListener(jdbcTemplate), jdbcTemplate);
    }

    @Test
    public void testWrite() throws Exception {
        // Given
        Client client = Client.builder().id(1L).build();
        Member member = Member.builder().client(client).build();
        Contract contract = Contract.builder().member(member).build();
        PaymentStatus paymentStatus = PaymentStatus.builder().id(PAYMENT_STATUS_COMPLETED).build();

        Invoice invoice = Invoice.builder()
                .id(1L)
                .contract(contract)
                .chargeAmount(1000L)
                .paymentStatus(paymentStatus)
                .isDeleted(false)
                .build();

        Chunk<Invoice> chunk = new Chunk<>(List.of(invoice));

        // MonthlyInvoiceStatisticsListener를 스파이로 설정
        MonthlyInvoiceStatisticsListener spyStatisticsListener = Mockito.spy(new MonthlyInvoiceStatisticsListener(jdbcTemplate));

        // Reflection을 사용하여 clientStatisticsMap 필드에 접근
        Field field = CustomMonthlyInvoiceWriter.class.getDeclaredField("clientStatisticsMap");
        field.setAccessible(true);

        // 맵을 가져와서 스파이를 추가
        Map<Long, MonthlyInvoiceStatisticsListener> clientStatisticsMap = (Map<Long, MonthlyInvoiceStatisticsListener>) field.get(writer);
        clientStatisticsMap.put(client.getId(), spyStatisticsListener);


        writer.write(chunk);


        verify(spyStatisticsListener, times(1)).setClientId(client.getId());
        verify(spyStatisticsListener, times(1)).addInvoice(invoice.getChargeAmount());
        verify(spyStatisticsListener, times(1)).addCollected(invoice.getChargeAmount());
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

        Chunk<Invoice> chunk = new Chunk<>(List.of(invoice));

        // 스파이 설정
        MonthlyInvoiceStatisticsListener spyStatisticsListener = Mockito.spy(new MonthlyInvoiceStatisticsListener(jdbcTemplate));

        // Reflection을 사용하여 clientStatisticsMap 필드에 접근
        Field field = CustomMonthlyInvoiceWriter.class.getDeclaredField("clientStatisticsMap");
        field.setAccessible(true);

        // 맵을 가져와서 스파이를 추가
        Map<Long, MonthlyInvoiceStatisticsListener> clientStatisticsMap = (Map<Long, MonthlyInvoiceStatisticsListener>) field.get(writer);
        clientStatisticsMap.put(client.getId(), spyStatisticsListener);


        writer.write(chunk);


        verify(spyStatisticsListener, times(1)).setClientId(client.getId());
        verify(spyStatisticsListener, never()).addInvoice(anyLong());
        verify(spyStatisticsListener, never()).addCollected(anyLong());
        verify(spyStatisticsListener, never()).addOutstanding(anyLong());
    }
}