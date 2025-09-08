package team.shdsesc.stocksimul.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private int level;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<String> tickerList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roleSet = new HashSet<>();

    public static UserDTO toUsersDTO(UserEntity userEntity) {
        return new UserDTO(
                userEntity.getUserId(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getLevel(),
                userEntity.getTickerList(),
                userEntity.getRoleSet().stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .toList()
        );
    }

    public void addMemberRole(UserRole role) {
        roleSet.add(role);
    }
}
