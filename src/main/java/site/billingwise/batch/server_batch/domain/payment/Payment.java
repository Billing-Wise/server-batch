package site.billingwise.batch.server_batch.domain.payment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment extends BaseEntity {

    @Id
    @Column(name = "invoice_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(length = 50, nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private Long payAmount;

    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
    private PaymentAccount paymentAccount;

    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
    private PaymentCard paymentCard;

}