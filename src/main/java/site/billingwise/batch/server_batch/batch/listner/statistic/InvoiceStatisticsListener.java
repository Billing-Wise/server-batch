package site.billingwise.batch.server_batch.batch.listner.statistic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import site.billingwise.batch.server_batch.batch.util.StatusConstants;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
public abstract class InvoiceStatisticsListener implements StepExecutionListener {

    protected abstract Long getStatisticsType();

    private final JdbcTemplate jdbcTemplate;

    private long totalInvoicedMoney;
    private long totalCollectedMoney;
    private long totalOutstanding;
    private Long clientId;

    private boolean saveInvoked = false;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // Step 종료 후 통계가 아직 저장되지 않았다면 저장
        if (!saveInvoked && totalInvoicedMoney > 0) {
            saveStatistics();
        }
        return ExitStatus.COMPLETED;
    }

    public void saveStatistics() {
        saveInvoked = true;
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfPeriod;
        int periodNumber;

        // 월간 또는 주간 통계에 따라 시작일 및 기간 번호 설정
        if (getStatisticsType() == StatusConstants.STATISTICS_TYPE_MONTHLY) {
            startOfPeriod = today.minusMonths(1).withDayOfMonth(1);
            periodNumber = 0;
        } else {
            startOfPeriod = today.minusWeeks(1).with(DayOfWeek.MONDAY);
            periodNumber = startOfPeriod.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        }

        updateInvoiceStatics(jdbcTemplate, totalInvoicedMoney, totalCollectedMoney, totalOutstanding, startOfPeriod, periodNumber);
    }

    // 청구서 통계 데이터베이스 업데이트
    private void updateInvoiceStatics(JdbcTemplate jdbcTemplate, long totalInvoicedMoney, long totalCollectedMoney, long totalOutstanding, LocalDateTime startOfPeriod, int periodNumber) {

        if (clientId == null) {
            log.info("invoice 테이블에 invoice 데이터 없음");
            return;
        }

        String sql = "insert into invoice_statistics (reference_date, total_invoiced, total_collected, outstanding, type_id, year, month, week, client_id, is_deleted, created_at, updated_at ) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false, now(), now())";
        jdbcTemplate.update(sql, startOfPeriod,
                totalInvoicedMoney,
                totalCollectedMoney,
                totalOutstanding,
                getStatisticsType(),
                startOfPeriod.getYear(),
                startOfPeriod.getMonthValue(),
                periodNumber,
                clientId
        );
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        resetStatistics();
    }

    // 통계 초기화
    public void resetStatistics() {
        totalInvoicedMoney = 0;
        totalCollectedMoney = 0;
        totalOutstanding = 0;
        clientId = null;
        saveInvoked = false;
    }

    // 청구 금액 추가
    public void addInvoice(long invoice) {
        totalInvoicedMoney += invoice;
    }
    // 수금액 추가
    public void addCollected(long collected) {
        totalCollectedMoney += collected;
    }
    // 미수금액 추가
    public void addOutstanding(long outstanding) {
        totalOutstanding += outstanding;
    }
    // 클라이언트 ID 설정
    public void setClientId(Long id) {
        this.clientId = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public long getTotalInvoicedMoney() {
        return totalInvoicedMoney;
    }
}