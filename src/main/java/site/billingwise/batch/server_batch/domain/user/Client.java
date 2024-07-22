package site.billingwise.batch.server_batch.domain.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import site.billingwise.batch.server_batch.domain.common.BaseEntity;
import site.billingwise.batch.server_batch.domain.item.Item;
import site.billingwise.batch.server_batch.domain.member.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 60, unique = true, nullable = false)
    private String authCode;

    @Column(length = 20, nullable = false)
    private String phone;

    @OneToMany(mappedBy = "client")
    private List<User> userList = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<Member> memberList = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<Item> itemList = new ArrayList<>();

}