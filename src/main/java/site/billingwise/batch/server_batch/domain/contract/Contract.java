package site.billingwise.batch.server_batch.domain.contract;

import jakarta.persistence.*;
import lombok.*;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.InvoiceType;
import site.billingwise.batch.server_batch.domain.item.Item;
import site.billingwise.batch.server_batch.domain.member.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Contract extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_type_id", nullable = false)
    private InvoiceType invoiceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_status_id", nullable = false)
    private ContractStatus contractStatus;

    @Column(nullable = false)
    private Boolean isSubscription;

    @Column(nullable = false)
    private Long itemPrice;

    @Column(nullable = false)
    private Integer itemAmount;

    @Column(nullable = false)
    private Integer contractCycle;

    @Column(nullable = false)
    private Integer paymentDueCycle;

    @Column(nullable = false)
    private Boolean isEasyConsent;

    @OneToMany(mappedBy = "contract")
    private List<Invoice> invoiceList = new ArrayList<>();

}
