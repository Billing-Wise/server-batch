package site.billingwise.batch.server_batch.feign;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayClientResponse {
    private int statusCode;
    private String message;
}