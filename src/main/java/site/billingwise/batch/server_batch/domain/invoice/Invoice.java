package site.billingwise.batch.server_batch.domain.invoice;

import jakarta.persistence.*;
import lombok.*;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.contract.PaymentType;
import site.billingwise.batch.server_batch.domain.payment.Payment;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_type_id", nullable = false)
    private InvoiceType invoiceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_status_id", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private Long chargeAmount;

    @Column(nullable = false)
    private LocalDateTime contractDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @OneToOne(mappedBy = "invoice", fetch = FetchType.LAZY)
    private Payment payment;
}
