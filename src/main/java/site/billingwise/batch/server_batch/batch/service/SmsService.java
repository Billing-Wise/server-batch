package site.billingwise.batch.server_batch.batch.service;

import jakarta.annotation.PostConstruct;
import net.nurigo.sdk.message.model.Message;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SmsService {

    private DefaultMessageService messageService;

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecretKey;

    @Value("${coolsms.api.url}")
    private String url;

    @PostConstruct
    private void init(){
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, url);
    }

    public SingleMessageSentResponse sendSuccessBilling(String to, String owner, String bank, Integer itemPrice) {
        LocalDateTime now = LocalDateTime.now();

        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        Message message = new Message();

        message.setFrom("010-6642-2113");
        message.setTo(to);

        String text = """
                안녕하세요. [빌링와이즈] 입니다.
                %d년 %d월 %d일 고객님의 정기 결제가 성공적으로 이뤄졌습니다.
                은행: %s
                예금주: %s
                총 금액: %d
                """.formatted(year, month, day, bank, owner, itemPrice);

        message.setText(text);


        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return response;
    }

    public SingleMessageSentResponse sendFailBilling(String to, String owner, String bank, Integer itemPrice, String failMessage) {
        LocalDateTime now = LocalDateTime.now();

        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        Message message = new Message();

        message.setFrom("010-6642-2113");
        message.setTo(to);

        String text = """
                안녕하세요. [빌링와이즈] 입니다.
                %d년 %d월 %d일 고객님의 정기 결제가 이뤄지지 않았습니다.
                은행: %s
                예금주: %s
                총 금액: %d
                실패 원인: %s
                """.formatted(year, month, day, bank, owner, itemPrice, failMessage);

        message.setText(text);


        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return response;
    }

    public SingleMessageSentResponse sendInvoice(String to, String owner, String bank, Integer itemPrice, Long invoiceId) {
        LocalDateTime now = LocalDateTime.now();

        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        Message message = new Message();

        message.setFrom("010-6642-2113");
        message.setTo(to);


        String text = """
                안녕하세요. [빌링와이즈] 입니다.
                %d년 %d월 %d일 고객님의 청구서 보내드립니다.

                납부 금액
                금액: %d
                납부 링크
                www.billingwise.site/m/payment/%d/info
                """.formatted(year, month, day, itemPrice, invoiceId);


        message.setText(text);


        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return response;
    }

}
