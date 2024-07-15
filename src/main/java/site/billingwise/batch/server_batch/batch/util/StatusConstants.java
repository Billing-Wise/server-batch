package site.billingwise.batch.server_batch.batch.util;

public class StatusConstants {

    // 계약 상태
    public static final long CONTRACT_STATUS_PENDING = 1L; // 대기
    public static final long CONTRACT_STATUS_IN_PROGRESS = 2L; // 진행
    public static final long CONTRACT_STATUS_TERMINATED = 3L; // 종료

    // 납부 상태
    public static final long PAYMENT_STATUS_UNPAID = 1L;   // 미납
    public static final long PAYMENT_STATUS_COMPLETED = 2L;   // 완납
    public static final long PAYMENT_STATUS_PENDING = 3L;   // 대기

    // 청구 타입
    public static final long INVOICE_TYPE_AUTOMATIC_BILLING = 1L;   // 자동청구
    public static final long INVOICE_TYPE_MANUAL_BILLING = 2L;   // 수동청구

    // 결제 수단
    public static final long PAYMENT_TYPE_PAYER_PAYMENT = 1L;   // 납부자 결제
    public static final long PAYMENT_TYPE_AUTOMATIC_TRANSFER = 2L;   // 자동 이체

}
