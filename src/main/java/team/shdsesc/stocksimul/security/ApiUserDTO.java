package team.shdsesc.stocksimul.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
@ToString
public class ApiUserDTO extends User {
    private String client_id;
    private String client_secret;

    public ApiUserDTO(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.client_id = username;
        this.client_secret = password;
    }
}
