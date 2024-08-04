package site.billingwise.batch.server_batch.batch.listner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.domain.contract.Contract;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSkipListener implements SkipListener<Contract, Contract> {


    @Override
    public void onSkipInRead(Throwable t) {
        log.error("CustomSkipListener onSkipInRead 데이터 read에서 error발생: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Contract item, Throwable t) {
        log.error("CustomSkipListener onSkipInWrite Skip 된 Writing item 데이터 :  ID={}, Error: {}", item.getId(), t.getMessage());
    }

    @Override
    public void onSkipInProcess(Contract item, Throwable t) {
        log.error("CustomSkipListener onSkipInProcess Skip 된 Processing item 데이터 :  ID={}, Error: {}", item.getId(), t.getMessage());
    }
}
