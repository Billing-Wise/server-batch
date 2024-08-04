package site.billingwise.batch.server_batch.batch.policy.backoff;

import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

public class CustomBackOffPolicy implements BackOffPolicy {

    private final long initialInterval;
    private final double multiplier;
    private final long maxInterval;


    public CustomBackOffPolicy(long initialInterval, double multiplier, long maxInterval) {
        this.initialInterval = initialInterval;
        this.multiplier = multiplier;
        this.maxInterval = maxInterval;
    }

    // 일단 시작할 때는 초기값으로 설정
    @Override
    public BackOffContext start(RetryContext retryContext) {
        return new CustomBackOffContext(initialInterval);
    }

    @Override
    public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
        CustomBackOffContext context = (CustomBackOffContext) backOffContext;

        // 증가된 대기 시간 계산
        long nextInterval = (long) (context.getInterval() * multiplier);

        // 최대 대기 시간 초과 시 제한
        if (nextInterval > maxInterval) {
            nextInterval = maxInterval;
        }

        context.setInterval(nextInterval);

        try {
            // 스레드 대기상태
            Thread.sleep(context.getInterval());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackOffInterruptedException("Thread interrupted during backOff", e);
        }
    }

    // BackOffContext 구현 클래스
    private static class CustomBackOffContext implements BackOffContext {
        private long interval;

        public CustomBackOffContext(long initialInterval) {
            this.interval = initialInterval;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }
    }
}