package site.billingwise.batch.server_batch.batch.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.member.ConsentAccount;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;


    // 결제 실패
    public void sendPaymentFailMailCode(String email, Invoice invoice, ConsentAccount consentAccount) {
        try {
            MimeMessage message = createFailMail(email, invoice, consentAccount);
            mailSender.send(message);
            log.info("결제 실패 이메일이 성공적으로 전송되었습니다. 수신자: {}", email);
        } catch (MessagingException e) {
            log.error("결제 실패 이메일 전송 중 오류가 발생했습니다. 수신자: {}", email, e);
        }
    }

    // 결제 성공
    public void sendPaymentSuccessMailCode(String email, Invoice invoice, ConsentAccount consentAccount) {
        try {
            MimeMessage message = createSuccessMail(email, invoice, consentAccount);
            mailSender.send(message);
            log.info("결제 성공 이메일이 성공적으로 전송되었습니다. 수신자: {}", email);
        } catch (MessagingException e) {
            log.error("결제 성공 이메일 전송 중 오류가 발생했습니다. 수신자: {}", email, e);
        }
    }

    // 청구서 발송
    public void sendInvoiceMail(String email, Invoice invoice) {
        try {
            MimeMessage message = createInvoiceMail(email, invoice);
            mailSender.send(message);
            log.info("청구서 이메일이 성공적으로 전송되었습니다. 수신자: {}", email);
        } catch (MessagingException e) {
            log.error("청구서 이메일 전송 중 오류가 발생했습니다. 수신자: {}", email, e);
        }
    }

    private MimeMessage createSuccessMail(String email, Invoice invoice, ConsentAccount consentAccount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[빌링와이즈] 결제 성공");
        String body = "<h1>안녕하세요, 빌링와이즈 입니다.</h1>"
                + "<p>결제가 성공적으로 처리되었습니다.</p>"
                + "<p>청구 금액: " + invoice.getChargeAmount() + "</p>"
                + "<p>은행: " + consentAccount.getBank() + "</p>"
                + "<p>예금주: " + consentAccount.getOwner() + "</p>"
                + "<p>계좌번호: " + consentAccount.getNumber() + "</p>"
                + "<p>고객 문의 번호: " + "010-xxxx-xxxx " + "</p>";
        helper.setText(body, true);

        return message;
    }

    private MimeMessage createFailMail(String email, Invoice invoice, ConsentAccount consentAccount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[빌링와이즈] 결제 실패");
        String body = "<h1>안녕하세요, 빌링와이즈 입니다.</h1>"
                + "<p>결제가 실패하였습니다.</p>"
                + "<p>청구 금액: " + invoice.getChargeAmount() + "</p>"
                + "<p>은행: " + consentAccount.getBank() + "</p>"
                + "<p>예금주: " + consentAccount.getOwner() + "</p>"
                + "<p>계좌번호: " + consentAccount.getNumber() + "</p>"
                + "<p>고객 문의 번호: " + "010-xxxx-xxxx " + "</p>";
        helper.setText(body, true);

        return message;
    }

    private MimeMessage createInvoiceMail(String email, Invoice invoice ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[빌링와이즈] 청구서 발송");
        String body = "<h1>안녕하세요, 빌링와이즈 입니다.</h1>"
                + "<p>청구서가 발송되었습니다.</p>"
                + "<p>청구 금액: " + invoice.getChargeAmount() + "</p>"
                + "<p>고객 문의 번호: " + "010-xxxx-xxxx " + "</p>";
        // url 만들 예정
        helper.setText(body, true);

        return message;
    }
}
