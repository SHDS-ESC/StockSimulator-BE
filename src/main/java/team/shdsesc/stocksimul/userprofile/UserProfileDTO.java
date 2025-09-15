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

    public UserProfileDTO toUserProfileEntity(UserProfileEntity entity) {
        return UserProfileDTO.builder()
                .id(entity.getUserProfileId())
                .totalInvested(entity.getTimeLine().getSeedMoney())
                .totalAssets(entity.getTimeLine().getSeedMoney() - entity.getCashBalance())
                .cashBalance(entity.getCashBalance())
                .nickname(entity.getNickname())
                .name(entity.getTimeLine().getName())
                .state(entity.getState())
                .build();
    }
}
