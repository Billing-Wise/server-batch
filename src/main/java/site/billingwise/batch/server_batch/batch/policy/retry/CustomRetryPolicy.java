package site.billingwise.batch.server_batch.batch.policy.retry;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.stereotype.Component;

@Component
public class CustomRetryPolicy implements RetryPolicy {

    private static final int MAX_ATTEMPTS = 3;

    @Override
    public boolean canRetry(RetryContext retryContext) {
        return retryContext.getRetryCount() < MAX_ATTEMPTS;
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext retryContext) {

    }

    @Override
    public void registerThrowable(RetryContext retryContext, Throwable throwable) {
        ((RetryContextSupport) retryContext).registerThrowable(throwable);
    }
}