package site.billingwise.batch.server_batch.batch.generateinvoice.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.invoice.InvoiceType;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcGenerateInvoiceWriterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private JdbcGenerateInvoiceWriter jdbcGenerateInvoiceWriter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("청구 생성(Write) 로직 테스트")
    void testWrite() throws Exception {
        // Given
        InvoiceType autoInvoiceType = InvoiceType.builder()
                .id(1L)
                .name("자동청구")
                .build();

        InvoiceType manualInvoiceType = InvoiceType.builder()
                .id(2L)
                .name("수동청구")
                .build();

        PaymentType paymentType = PaymentType.builder()
                .id(1L)
                .name("납부자 결제")
                .isBasic(true)
                .build();

        Contract contract = Contract.builder()
                .id(1L)
                .contractCycle(15)
                .itemPrice(1000L)
                .itemAmount(2)
                .paymentType(paymentType)
                .invoiceType(autoInvoiceType)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Contract> contracts = Arrays.asList(contract);

        PaymentStatus unpaidPaymentStatus = PaymentStatus.builder()
                .id(1L)
                .name("미납")
                .build();

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(unpaidPaymentStatus);

        // When
        jdbcGenerateInvoiceWriter.write(new Chunk<>(contracts));

        // Then
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> pssCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        verify(jdbcTemplate, times(1)).batchUpdate(queryCaptor.capture(), pssCaptor.capture());

        BatchPreparedStatementSetter pss = pssCaptor.getValue();
        assertNotNull(pss);

        String expectedSql = "insert into invoice (contract_id, invoice_type_id, payment_type_id, payment_status_id, charge_amount, contract_date, due_date, is_deleted, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, false, NOW(), NOW())";

        assertEquals(expectedSql, queryCaptor.getValue());
    }
}