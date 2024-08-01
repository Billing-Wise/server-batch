package site.billingwise.batch.server_batch.batch.invoiceprocessing.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.member.ConsentAccount;
import site.billingwise.batch.server_batch.domain.member.Member;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceRowMapper implements RowMapper<Invoice> {

    @Override
    public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {

        ConsentAccount consentAccount = ConsentAccount.builder()
                .number(rs.getString("number"))
                .bank(rs.getString("bank"))
                .owner(rs.getString("owner"))
                .build();

        Member member = Member.builder()
                .id(rs.getLong("member_id"))
                .email(rs.getString("email"))
                .name(rs.getString("name"))
                .phone(rs.getString("phone"))
                .consentAccount(consentAccount)
                .build();

        Contract contract = Contract.builder()
                .id(rs.getLong("contract_id"))
                .member(member)
                .isSubscription(rs.getBoolean("is_subscription"))
                .build();

        PaymentType paymentType = PaymentType.builder()
                .id(rs.getLong("payment_type_id"))
                .build();

        return Invoice.builder()
                .id(rs.getLong("invoice_id"))
                .contract(contract)
                .chargeAmount(rs.getLong("charge_amount"))
                .contractDate(rs.getTimestamp("contract_date").toLocalDateTime())
                .dueDate(rs.getTimestamp("due_date").toLocalDateTime())
                .paymentType(paymentType)
                .isDeleted(rs.getBoolean("is_deleted"))
                .build();
    }
}