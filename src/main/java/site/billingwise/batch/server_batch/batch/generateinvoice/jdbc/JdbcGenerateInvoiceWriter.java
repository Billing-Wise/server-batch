package site.billingwise.batch.server_batch.batch.generateinvoice.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.INVOICE_TYPE_MANUAL_BILLING;
import static site.billingwise.batch.server_batch.batch.util.StatusConstants.PAYMENT_TYPE_PAYER_PAYMENT;


@Component
@RequiredArgsConstructor
public class JdbcGenerateInvoiceWriter implements ItemWriter<Contract> {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public void write(Chunk<? extends Contract> chunk) throws Exception {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonth = now.plusMonths(1);
        int nextMonthValue = nextMonth.getMonthValue();
        int yearValue = nextMonth.getYear();

        PaymentStatus pendingPaymentStatus = findPendingPaymentStatus();

        List<Invoice> invoices = new ArrayList<>();


        for(Contract contract : chunk) {
            // 수동 청구면 pass(애초에 계약이 수동 청구인 경우)
            if(INVOICE_TYPE_MANUAL_BILLING == contract.getInvoiceType().getId()) {
                continue;
            }

            if(contract.getIsDeleted()){
                continue;
            }


            // 청구가 이미 만들어져 있으면, pass( 원래는 자동 청구인데, 단발성으로 청구를 생성한 경우 )
            if(!invoiceExists(contract, nextMonthValue, yearValue)){
                // 약정일
                LocalDateTime setInvoiceDate = LocalDateTime.of(yearValue, nextMonthValue, contract.getContractCycle(), 0, 0);
                // 결제기한
                LocalDateTime payDueDate = calculateDueDate(contract, setInvoiceDate);

                Invoice invoice = Invoice.builder()
                        .contract(contract)
                        .invoiceType(contract.getInvoiceType())
                        .paymentType(contract.getPaymentType())
                        .paymentStatus(pendingPaymentStatus)
                        .chargeAmount(contract.getItemPrice() * contract.getItemAmount())
                        .contractDate(setInvoiceDate)
                        .dueDate(payDueDate)
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                invoices.add(invoice);

            }
        }

        if (!invoices.isEmpty()) {
            bulkInsertInvoices(invoices);
        }
    }

    private void bulkInsertInvoices(List<Invoice> invoices) {
        String sql = "insert into invoice (contract_id, invoice_type_id, payment_type_id, payment_status_id, charge_amount, contract_date, due_date, is_deleted, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, ?, ?, false, NOW(), NOW())";

        jdbcTemplate.batchUpdate(sql,new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Invoice invoice = invoices.get(i);
                ps.setLong(1, invoice.getContract().getId());
                ps.setLong(2, invoice.getInvoiceType().getId());
                ps.setLong(3, invoice.getPaymentType().getId());
                ps.setLong(4, invoice.getPaymentStatus().getId());
                ps.setLong(5, invoice.getChargeAmount());
                ps.setTimestamp(6, java.sql.Timestamp.valueOf(invoice.getContractDate()));
                ps.setTimestamp(7, java.sql.Timestamp.valueOf(invoice.getDueDate()));
            }

            @Override
            public int getBatchSize() {
                return invoices.size();
            }
        });
    }

    private PaymentStatus findPendingPaymentStatus() {
        String sql = "select payment_status_id, name from payment_status where name = '대기'";
        return jdbcTemplate.queryForObject(sql, (ResultSet rs, int rowNum) ->
                PaymentStatus.builder()
                        .id(rs.getLong("payment_status_id"))
                        .build());
    }

    private LocalDateTime calculateDueDate(Contract contract, LocalDateTime setInvoiceDate) {
        if (PAYMENT_TYPE_PAYER_PAYMENT == contract.getPaymentType().getId()) {
            return setInvoiceDate.plusDays(3);
        }
        return setInvoiceDate;
    }

    private boolean invoiceExists(Contract contract, int month, int year) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0,0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        String sql = "select count(*) from invoice where contract_id = ? " +
                "and contract_date >= ? and contract_date <= ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{contract.getId(), startDate, endDate}, Integer.class);
        return count != null && count > 0;
    }
}
