package site.billingwise.batch.server_batch.batch.listner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

import static site.billingwise.batch.server_batch.batch.util.StatusConstants.STATISTICS_TYPE_WEEKLY;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceStatisticsListener implements StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    private long totalInvoicedMoney ;
    private long totalCollectedMoney;
    private long totalOutstanding;
    private Long clientId;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("afterStep 호출");
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfLastWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        int weekNumber = startOfLastWeek.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        updateinvoiceStatics(jdbcTemplate,totalInvoicedMoney, totalCollectedMoney, totalOutstanding, startOfLastWeek, weekNumber);

        return ExitStatus.COMPLETED;
    }

    private void updateinvoiceStatics(JdbcTemplate jdbcTemplate , long totalInvoicedMoney, long totalCollectedMoney, long totalOutstanding, LocalDateTime startOfLastWeek, int weekNumber) {
        // 데이터 베이스에 insert하는 로직
        // 참고날짜, 총 청구액, 총 수금액, 총 미납액, 타입상태(주간 or 월간), 년, 월, 주,
        log.info("업데이트 invoice statistics 테이블: totalInvoicedMoney={}, totalCollectedMoney={}, totalOutstanding={}, startOfLastWeek={}, weekNumber={}",
                totalInvoicedMoney, totalCollectedMoney, totalOutstanding, startOfLastWeek, weekNumber);


        String sql = "insert into invoice_statistics (reference_date, total_invoiced, total_collected, outstanding, type_id, year, month, week, client_id, is_deleted, created_at, updated_at ) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false, now(), now())";
        jdbcTemplate.update(sql,  startOfLastWeek,
                totalInvoicedMoney,
                totalCollectedMoney,
                totalOutstanding,
                STATISTICS_TYPE_WEEKLY,
                startOfLastWeek.getYear(),
                startOfLastWeek.getMonthValue(),
                weekNumber,
                clientId
        );

    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        totalInvoicedMoney = 0;
        totalCollectedMoney = 0;
        totalOutstanding = 0;
        clientId = null;
    }

    public void addInvoice(long Invoice) {
        totalInvoicedMoney += Invoice;
    }

    public void addCollected(long Collected) {
        totalCollectedMoney += Collected;
    }

    public void addOutstanding(long outstanding) {
        totalOutstanding += outstanding;
    }

    public void setClientId(Long id) {
        this.clientId = id;
    }

    public Long getClientId() {
        return clientId;
    }
}
