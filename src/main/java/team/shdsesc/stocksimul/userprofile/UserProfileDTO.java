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
    private Double totalInvested;
    private Double totalAssets;
    private Double cashBalance;
    @Builder.Default
    private String nickname ="";
    @Builder.Default
    private String name ="";
    private Integer state;
    // Timeline details for FE routing and chart mode
    private Long timelineId;
    private Integer timelineType;
    private LocalDateTime timelineFrom;
    private LocalDateTime timelineTo;
    private LocalDate processDate;
    private Double seedMoney;
}
