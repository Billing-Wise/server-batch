package site.billingwise.batch.server_batch.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "PayClient", url = "${pay.api.url}")
public interface PayClient {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    PayClientResponse pay(@RequestParam String type, @RequestParam String number);

}