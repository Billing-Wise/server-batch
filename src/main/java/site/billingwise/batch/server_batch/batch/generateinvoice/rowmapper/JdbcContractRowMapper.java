package site.billingwise.batch.server_batch.batch.generateinvoice.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.invoice.InvoiceType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcContractRowMapper implements RowMapper<Contract> {
    @Override
    public Contract mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Contract.builder()
                .id(rs.getLong("contract_id"))
                .itemPrice(rs.getLong("item_price"))
                .itemAmount(rs.getInt("item_amount"))
                .contractCycle(rs.getInt("contract_cycle"))
                .paymentDueCycle(rs.getInt("payment_due_cycle"))
                .isDeleted(rs.getBoolean("is_deleted"))
                .paymentType(
                        PaymentType.builder()
                                .id(rs.getLong("payment_type_id"))
                                .name(rs.getString("payment_type_name"))
                                .build()
                )
                .invoiceType(
                        InvoiceType.builder()
                                .id(rs.getLong("invoice_type_id"))
                                .name(rs.getString("invoice_type_name"))
                                .build()
                )
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .isDeleted(rs.getBoolean("is_deleted"))
                .build();
    }
}