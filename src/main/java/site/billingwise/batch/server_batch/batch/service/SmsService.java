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

        StringBuilder text = new StringBuilder();
        text.append(" 안녕하세요. [빌링와이즈] 입니다.\n");
        text.append(" " + year +"년 " + month + "월 " + day + "일 " + "고객님의 정기 결제가 성공적으로 이뤄졌습니다.\n");
        text.append(" 은행 : " + bank + "\n" );
        text.append(" 예금주 : " + owner + "\n" );
        text.append(" 총 금액 : " + itemPrice );

        message.setText(text.toString());


        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return response;
    }

    public SingleMessageSentResponse sendFailBilling(String to, String owner, String bank, Integer itemPrice) {
        LocalDateTime now = LocalDateTime.now();

        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        Message message = new Message();

        message.setFrom("010-6642-2113");
        message.setTo(to);

        StringBuilder text = new StringBuilder();
        text.append(" 안녕하세요. [빌링와이즈] 입니다.\n");
        text.append(" " + year +"년 " + month + "월 " + day + "일 " + "고객님의 정기 결제가 이뤄지지 않았습니다.\n");
        text.append(" 은행 : " + bank + "\n" );
        text.append(" 예금주 : " + owner + "\n" );
        text.append(" 총 금액 : " + itemPrice );

        message.setText(text.toString());


        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return response;
    }

    public SingleMessageSentResponse sendInvoice(String to, String owner, String bank, Integer itemPrice) {
        LocalDateTime now = LocalDateTime.now();

        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        Message message = new Message();

        message.setFrom("010-6642-2113");
        message.setTo(to);

        StringBuilder text = new StringBuilder();
        text.append(" 안녕하세요. [빌링와이즈] 입니다.\n");
        text.append(" " + year +"년 " + month + "월 " + day + "일 " + "고객님의 청구서 보내드립니다.\n");
        text.append("\n" );
        text.append("납부 금액\n");
        text.append(" 금액 : " + itemPrice + "\n");
        text.append("납부 링크\n");
        text.append("(link)");


        message.setText(text.toString());


        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        return response;
    }

}
