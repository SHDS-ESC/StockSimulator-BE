package team.shdsesc.stocksimul.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import team.shdsesc.stocksimul.auth.util.BaseEntity;

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
    private String usersEmail;

    @Column(nullable = false)
    private String usersPassword;

    private int usersLevel;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "users_ticker_list",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "ticker")
    @Builder.Default
    private List<String> tickerList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<UserRole> usersRoles = new HashSet<>();

    public static UserDTO toUsersDTO(UserEntity userEntity) {
        return new UserDTO(
                userEntity.getUsersId(),
                userEntity.getUsersEmail(),
                userEntity.getUsersPassword(),
                userEntity.getUsersLevel(),
                userEntity.getTickerList(),
                userEntity.getUsersRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .toList()
        );
    }

    public void addMemberRole(UserRole role) {
        usersRoles.add(role);
    }
}
