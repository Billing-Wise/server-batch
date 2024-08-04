package site.billingwise.batch.server_batch.domain.statics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;
import site.billingwise.batch.server_batch.domain.user.Client;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name="invoice_statistics")
public class InvoiceStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_date", nullable = false)
    private LocalDateTime date;

    @Column(name = "total_invoiced", nullable = false)
    private Long totalInvoiced;

    @Column(name = "total_collected", nullable = false)
    private Long totalCollected;

    @Column(name = "outstanding", nullable = false)
    private Long outstanding;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = true)
    private Integer month;

    @Column(nullable = true)
    private Integer week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private InvoiceStatisticsType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}