package site.billingwise.batch.server_batch.domain.contract;

import jakarta.persistence.*;
import lombok.*;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_type_id")
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "is_basic",nullable = false)
    private Boolean isBasic;

    @OneToMany(mappedBy = "paymentType")
    private List<Contract> contractList = new ArrayList<>();

    @OneToMany(mappedBy = "paymentType")
    private List<Invoice> invoiceList = new ArrayList<>();
}
