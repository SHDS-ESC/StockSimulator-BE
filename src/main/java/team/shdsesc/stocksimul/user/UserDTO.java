package team.shdsesc.stocksimul.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@ToString
public class UserDTO extends User {
    private Long userId;
    private String secretId;
    private String email;
    private String secretPassword;
    private int level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long lastProfileId;

    public UserDTO(LocalDateTime createdAt, LocalDateTime updatedAt, Long userId, String email, Long lastProfileId, String password, int level, Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.userId = userId;
        this.email = email;
        this.lastProfileId = lastProfileId;
        this.secretId = email;
        this.secretPassword = password;
        this.level = level;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
