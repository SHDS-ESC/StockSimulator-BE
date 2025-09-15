package team.shdsesc.stocksimul.userprofile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private Long id;
    private Long totalInvested;
    private Long totalAssets;
    private Long cashBalance;
    private String nickname ="";
    private String name ="";
    private Integer state;
}
