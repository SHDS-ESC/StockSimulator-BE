package team.shdsesc.stocksimul.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name="api_user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ApiUser {
    @Id
    private String client_id;
    private String client_secret;
}
