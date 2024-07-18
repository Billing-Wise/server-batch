package site.billingwise.batch.server_batch.batch.listner.statistic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.batch.util.StatusConstants;

@Component
@Slf4j
public class WeeklyInvoiceStatisticsListener extends InvoiceStatisticsListener {

    public WeeklyInvoiceStatisticsListener(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected Long getStatisticsType() {
        return StatusConstants.STATISTICS_TYPE_WEEKLY;
    }
}