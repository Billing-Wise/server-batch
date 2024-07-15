package site.billingwise.batch.server_batch.batch.invoiceprocessing.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.batch.service.EmailService;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.member.ConsentAccount;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceSendingAndPaymentManageWriter implements ItemWriter<Invoice> {

    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    @Override
    public void write(Chunk<? extends Invoice> chunk) {

        for(Invoice invoice : chunk) {

            if(invoice.getIsDeleted()) {
                continue;
            }
            // 자동 이체
            if(invoice.getPaymentType().getId() == INVOICE_TYPE_AUTOMATIC_BILLING) {

                ConsentAccount consentAccount = invoice.getContract().getMember().getConsentAccount();
                boolean paymentAttempt = false;

                if (consentAccount != null) {
                    // 결제 시도( 아직 개발되지 않았음 )
                    paymentAttempt = processAutoPayment(invoice);
                }

                // 결제 성공 시
                if (paymentAttempt) {

                    updatePaymentStatus(invoice.getId(), PAYMENT_STATUS_COMPLETED);
                    insertPaymentRecord(invoice, consentAccount);
                    emailService.sendPaymentSuccessMailCode(invoice.getContract().getMember().getEmail(), invoice, consentAccount);

                // 결제 실패 시
                } else {

                    emailService.sendPaymentFailMailCode(invoice.getContract().getMember().getEmail(), invoice, consentAccount);
                    updateFailPaymentStatus(invoice.getId());
                }

            // 납부자 결제
            } else if(invoice.getPaymentType().getId() == INVOICE_TYPE_MANUAL_BILLING){

                emailService.sendInvoiceMail(invoice.getContract().getMember().getEmail(), invoice);
                updatePaymentStatus(invoice.getId(), PAYMENT_STATUS_PENDING);
            }
        }
    }



    private void updateFailPaymentStatus(Long invoiceId) {
        String sql = "update invoice set updated_at = now() where invoice_id = ?";
        jdbcTemplate.update(sql,  invoiceId);
    }


    private void insertPaymentRecord(Invoice invoice, ConsentAccount consentAccount) {
        String sql = "insert into payment (invoice_id, payment_method, pay_amount, created_at, updated_at, is_deleted) values (?, ?, ?, NOW(), NOW(), false)";
        jdbcTemplate.update(sql, invoice.getId(), "계좌이체", invoice.getChargeAmount());
        // 납부 계좌 테이블에 데이터 넣기 ( 결재 성공의 경우 )
        insertPaymentAccount(invoice.getId(), consentAccount);
    }


    private void insertPaymentAccount(Long invoiceId, ConsentAccount consentAccount) {
        String sql = "insert into payment_account (invoice_id, number, bank, owner, created_at, updated_at, is_deleted) values (?, ?, ?, ?, now(), now(), false)";
        jdbcTemplate.update(sql, invoiceId, consentAccount.getNumber(), consentAccount.getBank(), consentAccount.getOwner());
    }


    private void updatePaymentStatus(Long invoiceId, long statusId) {
        String sql = "update invoice set payment_status_id = ?, updated_at = now() where invoice_id = ?";
        jdbcTemplate.update(sql, statusId, invoiceId);
    }

    //결제 시도
    private boolean processAutoPayment(Invoice invoice) {
        // 결제 시도 로직을 들어갈 곳

        return true;
    }
}
