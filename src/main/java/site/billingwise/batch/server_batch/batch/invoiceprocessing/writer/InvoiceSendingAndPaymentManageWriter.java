package site.billingwise.batch.server_batch.batch.invoiceprocessing.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.batch.service.EmailService;
import site.billingwise.batch.server_batch.batch.service.SmsService;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.member.ConsentAccount;
import site.billingwise.batch.server_batch.domain.member.Member;
import site.billingwise.batch.server_batch.feign.PayClient;
import site.billingwise.batch.server_batch.feign.PayClientResponse;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceSendingAndPaymentManageWriter implements ItemWriter<Invoice> {

    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PayClient payClient;

    @Override
    public void write(Chunk<? extends Invoice> chunk) {

        for (Invoice invoice : chunk) {

            processInvoice(invoice);
        }
    }

    private void processInvoice(Invoice invoice) {


        Contract contract = invoice.getContract();
        boolean checkSubscription = contract.getIsSubscription();
        long contract_id =  contract.getId();
        Member member = contract.getMember();


        log.info("Processing invoice ID: {}, Contract ID: {}, Member ID: {}", invoice.getId(), contract_id, member.getId());


        // 실시간 CMS(자동 결제)
        if(invoice.getPaymentType().getId() == PAYMENT_TYPE_AUTOMATIC_TRANSFER) {

            ConsentAccount consentAccount = invoice.getContract().getMember().getConsentAccount();
            PayClientResponse  paymentAttempt = null;

            if (consentAccount != null) {
                log.info("자동 결제 시도 invoice ID: {}, Account Number: {}", invoice.getId(), consentAccount.getNumber());
                paymentAttempt = processAutoPayment(invoice,consentAccount);
            } else {
                log.info("자동 결제 시도 안하는 invoice ID: {}", invoice.getId());
            }

            if (paymentAttempt != null && paymentAttempt.getStatusCode() ==200) {
                log.info("결제 성공한 invoice ID: {}", invoice.getId());
                updatePaymentStatus(invoice.getId(), PAYMENT_STATUS_COMPLETED);
                insertPaymentRecord(invoice, consentAccount);
                emailService.sendPaymentSuccessMailCode(invoice.getContract().getMember().getEmail(), invoice, consentAccount);
                smsService.sendSuccessBilling(member.getPhone(), member.getConsentAccount().getOwner(), member.getConsentAccount().getBank(), invoice.getChargeAmount().intValue());
//                 단건일 경우( 계약 종료로 변경 )
                if(!checkSubscription) {
                    updateNotSubscriptionContractStatus(contract_id, CONTRACT_STATUS_TERMINATED);
                }
                // 결제 실패 시
            } else {
                String paymentAttemptMessage = paymentAttempt != null ? paymentAttempt.getMessage() : "결제 시도 실패";
                log.info("결제 실패한 invoice ID: {}", invoice.getId());
                updatePaymentStatus(invoice.getId(), PAYMENT_STATUS_UNPAID);
                emailService.sendPaymentFailMailCode(invoice.getContract().getMember().getEmail(), invoice, consentAccount, paymentAttemptMessage);
                smsService.sendFailBilling(member.getPhone(), member.getConsentAccount().getOwner(), member.getConsentAccount().getBank(),
                        invoice.getChargeAmount().intValue(), paymentAttemptMessage);
                // 단건일 경우( 계약 종료로 변경 )
                if(!checkSubscription) {
                    updateNotSubscriptionContractStatus(contract_id, CONTRACT_STATUS_TERMINATED);
                }
            }

            // 납부자 결제
        } else if(invoice.getPaymentType().getId() == PAYMENT_TYPE_PAYER_PAYMENT){
            log.info("납부자 결제  invoice ID: {}", invoice.getId());
            emailService.sendInvoiceMail(invoice.getContract().getMember().getEmail(), invoice);
            updatePaymentStatus(invoice.getId(), PAYMENT_STATUS_PENDING);
            smsService.sendInvoice(member.getPhone(), member.getConsentAccount().getOwner(), member.getConsentAccount().getBank(), invoice.getChargeAmount().intValue(),invoice.getId());

            // 단건일 경우( 계약 종료로 변경 )
            if(!checkSubscription) {
                updateNotSubscriptionContractStatus(contract_id, CONTRACT_STATUS_TERMINATED);
            }
        }
    }

    // 단건 계약의 상태를 변경하는 메서드
    private void updateNotSubscriptionContractStatus(long contract_id, long contractStatusTerminated){
        String sql = "update contract set contract_status_id = ?, updated_at = NOW() where contract_id = ?";
        jdbcTemplate.update(sql, contractStatusTerminated, contract_id);
    }


    // 결제 성공 시 결제 기록 삽입
    private void insertPaymentRecord(Invoice invoice, ConsentAccount consentAccount) {
        String sql = "insert into payment (invoice_id, payment_method, pay_amount, created_at, updated_at, is_deleted) values (?, ?, ?, NOW(), NOW(), false)";
        jdbcTemplate.update(sql, invoice.getId(), "ACCOUNT", invoice.getChargeAmount());
        // 납부 계좌 테이블에 데이터 넣기 ( 결재 성공의 경우 )
        insertPaymentAccount(invoice.getId(), consentAccount);
    }

    // 결제 성공 시 결제 계좌 정보 삽입
    private void insertPaymentAccount(long invoiceId, ConsentAccount consentAccount) {
        String sql = "insert into payment_account (invoice_id, number, bank, owner, created_at, updated_at, is_deleted) values (?, ?, ?, ?, now(), now(), false)";
        jdbcTemplate.update(sql, invoiceId, consentAccount.getNumber(), consentAccount.getBank(), consentAccount.getOwner());
    }

    // 청구서의 결제 상태 업데이트
    private void updatePaymentStatus(long invoiceId, long statusId) {
        String sql = "update invoice set payment_status_id = ?, updated_at = now() where invoice_id = ?";
        jdbcTemplate.update(sql, statusId, invoiceId);
    }

    // 결제 시도
    private PayClientResponse processAutoPayment(Invoice invoice, ConsentAccount consentAccount) {
        String type = "account";
        String number = consentAccount.getNumber();
        return payClient.pay(type, number);
    }
}
