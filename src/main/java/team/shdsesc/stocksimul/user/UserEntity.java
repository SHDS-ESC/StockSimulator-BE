package team.shdsesc.stocksimul.user;

import jakarta.persistence.*;
import lombok.*;
import team.shdsesc.stocksimul.auth.util.BaseEntity;
import team.shdsesc.stocksimul.stock.StockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Users")
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private int level;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<UserRole> usersRoles = new HashSet<>();

    @Column(name = "last_profile_id")
    private Long lastProfileId;

    public void addMemberRole(UserRole role) {
        usersRoles.add(role);
    }
}
