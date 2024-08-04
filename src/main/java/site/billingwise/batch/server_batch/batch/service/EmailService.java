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
    public void sendPaymentFailMailCode(String email, Invoice invoice, ConsentAccount consentAccount, String failMessage) {
        try {
            MimeMessage message = createFailMail(email, invoice, consentAccount, failMessage);
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

    private MimeMessage     createSuccessMail(String email, Invoice invoice, ConsentAccount consentAccount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[빌링와이즈] 결제 성공");
        String body = """
            <html>
            <body style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #3498db; border-bottom: 2px solid #3498db; padding-bottom: 10px;">빌링와이즈 결제 성공</h2>
                   <p>안녕하세요, 빌링와이즈입니다.<br>
                   고객님의 결제가 성공적으로 처리되었습니다.</p>
                <ul style="background-color: #f9f9f9; padding: 15px; border-left: 4px solid #3498db;">
                    <li><strong>청구 금액:</strong> %,d원</li>
                   <li><strong>은행:</strong> %s</li>
                   <li><strong>예금주:</strong> %s</li>
                    <li><strong>계좌번호:</strong> %s</li>
               </ul>
                <p>결제와 관련하여 문의사항이 있으시면 아래 연락처로 문의해 주세요.</p>
               <p>고객 문의: 010-xxxx-xxxx</p>
                <hr>
                <small style="color: #777;">&copy; 2024 빌링와이즈. All rights reserved.</small>
            </body>
            </html>
            """.formatted(invoice.getChargeAmount(), consentAccount.getBank(), consentAccount.getOwner(), consentAccount.getNumber());
        helper.setText(body, true);

        return message;
    }

    private MimeMessage createFailMail(String email, Invoice invoice, ConsentAccount consentAccount,String failMessage) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[빌링와이즈] 결제 실패");
        String body = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2 style="color: #e74c3c;">빌링와이즈 결제 실패</h2>
                <p>안녕하세요, 빌링와이즈입니다.<br>
                죄송합니다. 고객님의 결제가 실패하였습니다.</p>
                <ul style="background-color: #f8f8f8; padding: 15px; border-left: 4px solid #e74c3c;">
                    <li>청구 금액: %,d원</li>
                    <li>은행: %s</li>
                    <li>예금주: %s</li>
                    <li>계좌번호: %s</li>
                    <li>실패 원인: %s</li>
                </ul>
                <p>결제 실패와 관련하여 문의사항이 있으시면 아래 연락처로 문의해 주세요.</p>
                <p>고객 문의: 010-xxxx-xxxx</p>
                <hr>
                <small>&copy; 2024 빌링와이즈. All rights reserved.</small>
            </body>
            </html>
            """.formatted(invoice.getChargeAmount(), consentAccount.getBank(), consentAccount.getOwner(), consentAccount.getNumber(), failMessage);
        helper.setText(body, true);

        return message;
    }

    private MimeMessage createInvoiceMail(String email, Invoice invoice ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[빌링와이즈] 청구서 발송");
        String body = """
            <html>
            <body style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #3498db; border-bottom: 2px solid #3498db; padding-bottom: 10px;">빌링와이즈 청구서 발송</h2>
                <p>안녕하세요, 빌링와이즈입니다.<br>
                청구서가 발송되었습니다.</p>
                <ul style="background-color: #f9f9f9; padding: 15px; border-left: 4px solid #3498db;">
                    <li><strong>청구 금액:</strong> %,d원</li>
                </ul>
                <p>청구서 상세 정보 확인: <a href="https://www.billingwise.site/m/payment/%d/info" style="color: #3498db;">청구서 보기</a></p>
                <p>문의사항이 있으시면 아래 연락처로 문의해 주세요.</p>
                <p>고객 문의: 010-xxxx-xxxx</p>
                <hr>
                <small style="color: #777;">&copy; 2024 빌링와이즈. All rights reserved.</small>
            </body>
            </html>
            """.formatted(invoice.getChargeAmount(), invoice.getId());
        helper.setText(body, true);

        return message;
    }
}
