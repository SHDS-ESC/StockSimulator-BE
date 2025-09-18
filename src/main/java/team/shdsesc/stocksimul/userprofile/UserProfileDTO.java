package team.shdsesc.stocksimul.userprofile;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private Long id;
    private Long totalInvested;
    private Long totalAssets;
    private Long cashBalance;
    @Builder.Default
    private String nickname ="";
    @Builder.Default
    private String name ="";
    private Integer state;

    private LocalDate processDate;
}
