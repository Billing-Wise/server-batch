package site.billingwise.batch.server_batch.batch.invoicestaticsprocessing.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;
import site.billingwise.batch.server_batch.domain.member.Member;
import site.billingwise.batch.server_batch.domain.user.Client;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StaticsInvoiceRowMapper implements RowMapper<Invoice> {
    @Override
    public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {

        Client client = Client.builder()
                .id(rs.getLong("client_id"))
                .build();

        Member member = Member.builder()
                .id(rs.getLong("member_id"))
                .client(client)
                .build();

        Contract contract = Contract.builder()
                .id(rs.getLong("contract_id"))
                .member(member)
                .build();

        PaymentType paymentType = PaymentType.builder()
                .id(rs.getLong("payment_type_id"))
                .build();

        PaymentStatus paymentStatus = PaymentStatus.builder()
                .id(rs.getLong("payment_status_id"))
                .build();


        return Invoice.builder()
                .id(rs.getLong("invoice_id"))
                .contract(contract)
                .chargeAmount(rs.getLong("charge_amount"))
                .contractDate(rs.getTimestamp("contract_date").toLocalDateTime())
                .dueDate(rs.getTimestamp("due_date").toLocalDateTime())
                .paymentType(paymentType)
                .paymentStatus(paymentStatus)
                .isDeleted(rs.getBoolean("is_deleted"))
                .build();

    }
}
