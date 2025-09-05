package team.shdsesc.stocksimul.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import team.shdsesc.stocksimul.auth.dto.ApiUserDTO;
import team.shdsesc.stocksimul.auth.dto.MemberRole;

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
public class ApiUser {
    @Id
    private String userId;
    private String password;
    private String phoneNumber;
    private int level;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<String> tickerList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Set<MemberRole> roleSet = new HashSet<>();

    public static ApiUserDTO toDto(ApiUser apiUser) {
        return new ApiUserDTO(
                apiUser.getUserId(),
                apiUser.getPassword(),
                apiUser.getPhoneNumber(),
                apiUser.getLevel(),
                apiUser.getTickerList(),
                apiUser.getRoleSet().stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .toList()
        );
    }

    public void addMemberRole(MemberRole role) {
        roleSet.add(role);
    }
}
