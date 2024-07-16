package site.billingwise.batch.server_batch.batch.generateinvoice.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.ContractStatus;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.invoice.InvoiceType;
import site.billingwise.batch.server_batch.domain.item.Item;
import site.billingwise.batch.server_batch.domain.member.Member;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcContractRowMapper implements RowMapper<Contract> {
    @Override
    public Contract mapRow(ResultSet rs, int rowNum) throws SQLException {

        Member member = Member.builder()
                .id(rs.getLong("member_id"))
                .build();


        Item item = Item.builder()
                .id(rs.getLong("item_id"))
                .build();


        InvoiceType invoiceType = InvoiceType.builder()
                .id(rs.getLong("invoice_type_id"))
                .build();


        PaymentType paymentType = PaymentType.builder()
                .id(rs.getLong("payment_type_id"))
                .build();

        ContractStatus contractStatus = ContractStatus.builder()
                .id(rs.getLong("contract_status_id"))
                .build();


        return Contract.builder()
                .id(rs.getLong("contract_id"))
                .member(member)
                .item(item)
                .invoiceType(invoiceType)
                .paymentType(paymentType)
                .contractStatus(contractStatus)
                .isSubscription(rs.getBoolean("is_subscription"))
                .itemPrice(rs.getLong("item_price"))
                .itemAmount(rs.getInt("item_amount"))
                .contractCycle(rs.getInt("contract_cycle"))
                .paymentDueCycle(rs.getInt("payment_due_cycle"))
                .isEasyConsent(rs.getBoolean("is_easy_consent"))
                .isDeleted(rs.getBoolean("is_deleted"))
                .build();
    }
}