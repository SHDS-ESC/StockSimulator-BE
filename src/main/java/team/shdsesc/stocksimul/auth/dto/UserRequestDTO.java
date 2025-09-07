package team.shdsesc.stocksimul.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserRequestDTO {
    private String email;
    private String password;
    private String level;
    private List<String> tickerList;
}
