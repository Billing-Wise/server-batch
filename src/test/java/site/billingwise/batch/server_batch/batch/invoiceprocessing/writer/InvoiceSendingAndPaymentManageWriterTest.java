package site.billingwise.batch.server_batch.batch.invoiceprocessing.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import site.billingwise.batch.server_batch.batch.service.EmailService;
import site.billingwise.batch.server_batch.batch.service.SmsService;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.member.ConsentAccount;
import site.billingwise.batch.server_batch.domain.member.Member;
import site.billingwise.batch.server_batch.feign.PayClient;
import site.billingwise.batch.server_batch.feign.PayClientResponse;

import java.lang.reflect.Method;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static site.billingwise.batch.server_batch.batch.util.StatusConstants.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceSendingAndPaymentManageWriterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private PayClient payClient;

    @InjectMocks
    private InvoiceSendingAndPaymentManageWriter writer;

    private Invoice invoice;

    @BeforeEach
    public void setUp() {
        Member member = Member.builder()
                .id(1L)
                .email("test@naver.com")
                .build();

        Contract contract = Contract.builder()
                .id(1L)
                .member(member)
                .build();

        PaymentType paymentType = PaymentType.builder()
                .id(PAYMENT_TYPE_AUTOMATIC_TRANSFER)
                .build();

        invoice = Invoice.builder()
                .id(1L)
                .isDeleted(false)
                .contract(contract)
                .paymentType(paymentType)
                .build();
    }

    @Test
    @DisplayName("결제 실패 시 수정 시각 업데이트")
    public void testUpdateFailPaymentStatus() throws Exception {
        Method method = InvoiceSendingAndPaymentManageWriter.class.getDeclaredMethod("updateFailPaymentStatus", long.class);
        method.setAccessible(true);
        method.invoke(writer, 1L);
        verify(jdbcTemplate).update("update invoice set updated_at = now() where invoice_id = ?", 1L);
    }

    @Test
    @DisplayName("결제 성공 시 납부 테이블 데이터 입력")
    public void testInsertPaymentRecord() throws Exception {
        ConsentAccount consentAccount = ConsentAccount.builder()
                .number("12345")
                .bank("bank")
                .owner("변현진")
                .build();

        Method method = InvoiceSendingAndPaymentManageWriter.class.getDeclaredMethod("insertPaymentRecord", Invoice.class, ConsentAccount.class);
        method.setAccessible(true);
        method.invoke(writer, invoice, consentAccount);
        verify(jdbcTemplate).update("insert into payment (invoice_id, payment_method, pay_amount, created_at, updated_at, is_deleted) values (?, ?, ?, NOW(), NOW(), false)", invoice.getId(), "ACCOUNT", invoice.getChargeAmount());
    }

    @Test
    @DisplayName("결제 성공 납부 계좌 테이블 데이터 입력")
    public void testInsertPaymentAccount() throws Exception {
        ConsentAccount consentAccount = ConsentAccount.builder()
                .number("12345")
                .bank("bank")
                .owner("변현진")
                .build();

        Method method = InvoiceSendingAndPaymentManageWriter.class.getDeclaredMethod("insertPaymentAccount", long.class, ConsentAccount.class); // long 타입으로 변경
        method.setAccessible(true);
        method.invoke(writer, 1L, consentAccount);
        verify(jdbcTemplate).update("insert into payment_account (invoice_id, number, bank, owner, created_at, updated_at, is_deleted) values (?, ?, ?, ?, now(), now(), false)", 1L, "12345", "bank", "변현진");
    }

    @Test
    @DisplayName("납부 상태 변경")
    public void testUpdatePaymentStatus() throws Exception {
        Method method = InvoiceSendingAndPaymentManageWriter.class.getDeclaredMethod("updatePaymentStatus", long.class, long.class);
        method.setAccessible(true);
        method.invoke(writer, 1L, PAYMENT_STATUS_COMPLETED);
        verify(jdbcTemplate).update("update invoice set payment_status_id = ?, updated_at = now() where invoice_id = ?", PAYMENT_STATUS_COMPLETED, 1L);
    }

    @Test
    @DisplayName("자동 결제 성공 테스트")
    public void testProcessAutoPayment() throws Exception {
        ConsentAccount consentAccount = ConsentAccount.builder()
                .number("12345")
                .build();

        when(payClient.pay("account", "12345")).thenReturn(PayClientResponse.builder().statusCode(200).build());

        Method method = InvoiceSendingAndPaymentManageWriter.class.getDeclaredMethod("processAutoPayment", Invoice.class, ConsentAccount.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(writer, invoice, consentAccount);

        assert result;
        verify(payClient).pay("account", "12345");
    }
}
