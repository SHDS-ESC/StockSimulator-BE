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
public class ApiUserDTO extends User {
    private String secretId;
    private String secretPassword;
    private String phoneNumber;
    private int level;
    private List<String> tickerList;

    public ApiUserDTO(String secretId, String secretPassword, String phoneNumber, int level, List<String> tickerList, Collection<? extends GrantedAuthority> authorities) {
        super(secretId, secretPassword, authorities);
        this.secretId = secretId;
        this.secretPassword = secretPassword;
        this.phoneNumber = phoneNumber;
        this.level = level;
        this.tickerList = tickerList;
    }
}
