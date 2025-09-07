package team.shdsesc.stocksimul.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString
public class UserDTO extends User {
    private Long userId;
    private String secretId;
    private String email;
    private String secretPassword;
    private String phoneNumber;
    private int level;
    private List<String> tickerList;

    public UserDTO(Long userId, String email, String password, String phoneNumber, int level, List<String> tickerList, Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.userId = userId;
        this.secretId = email;
        this.secretPassword = password;
        this.phoneNumber = phoneNumber;
        this.level = level;
        this.tickerList = tickerList;
    }
}
