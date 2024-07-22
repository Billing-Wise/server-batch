package site.billingwise.batch.server_batch.domain.member;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsentAccount extends BaseEntity {

    @Id
    @Column(name = "member_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 50, nullable = false)
    private String owner;

    @Column(length = 50, nullable = false)
    private String bank;

    @Column(length = 20, nullable = false)
    private String number;

    @Column(length = 512, nullable = false)
    private String signUrl;
}