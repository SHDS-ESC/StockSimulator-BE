package team.shdsesc.stocksimul.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import team.shdsesc.stocksimul.auth.dto.UserDTO;
import team.shdsesc.stocksimul.auth.dto.UserRole;

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
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private int level;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<String> tickerList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roleSet = new HashSet<>();

    public static UserDTO toUsersDTO(Users users) {
        return new UserDTO(
                users.getUserId(),
                users.getEmail(),
                users.getPassword(),
                users.getPhoneNumber(),
                users.getLevel(),
                users.getTickerList(),
                users.getRoleSet().stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .toList()
        );
    }

    public void addMemberRole(UserRole role) {
        roleSet.add(role);
    }
}
