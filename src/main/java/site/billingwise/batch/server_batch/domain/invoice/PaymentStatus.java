package site.billingwise.batch.server_batch.domain.invoice;

import jakarta.persistence.*;
import lombok.*;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_status_id")
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @OneToMany(mappedBy = "paymentStatus")
    private List<Invoice> invoiceList = new ArrayList<>();
}

