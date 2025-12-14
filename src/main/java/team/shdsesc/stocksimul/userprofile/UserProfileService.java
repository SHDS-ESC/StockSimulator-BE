package team.shdsesc.stocksimul.userprofile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.holdings.HoldingsRepository;
import team.shdsesc.stocksimul.user.UserEntity;
import team.shdsesc.stocksimul.user.UserRepository;
import team.shdsesc.stocksimul.util.FormatUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserProfileService {
    private final TimeLineRepository timeLineRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final HoldingsRepository holdingsRepository;
    private final FormatUtil formatUtil;

    public List<TimeLineEntity> getTimeLineList() {
        return timeLineRepository.findAll();
    }

    public List<UserProfileDTO> getUserProfileList(String email) {
        List<UserProfileEntity> userProfileList = userProfileRepository.findUserByUserEmail(email).orElseThrow(() -> new RuntimeException("UserProfile not found"));
        return userProfileList
                .stream()
                .map(this::toUserProfileDTO)
                .toList();
    }

    @Transactional
    public UserProfileDTO createUserProfile(CreateUserProfileDTO createUserProfileDTO) {
        UserEntity user = userRepository.findUserWithRolesByUserId(createUserProfileDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        TimeLineEntity timeLine = timeLineRepository.findById(createUserProfileDTO.getTimelineId()).orElseThrow(() -> new RuntimeException("Timeline not found"));
        UserProfileEntity userProfileEntity = toUserProfileEntity(createUserProfileDTO, user, timeLine);
        userProfileRepository.save(userProfileEntity);
        userRepository.updateCurrentProfileUser(user.getUsersId(), userProfileEntity.getUsersProfileId());
        //userProfileRepository.updateCurrentProfileState(userProfileEntity.getUserProfileId(), user.getUsersEmail());
        return toUserProfileDTO(userProfileEntity);
    }

    @Transactional
    public void updateUserProfile(UpdateUserProfileDTO updateUserProfileDTO) {
        UserEntity user = userRepository.findUserWithRolesByUserId(updateUserProfileDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        //userProfileRepository.updateCurrentProfileState(updateUserProfileDTO.getUserProfileId(),updateUserProfileDTO.getEmail());
        userRepository.updateCurrentProfileUser(user.getUsersId(), updateUserProfileDTO.getUserProfileId());
    }

    @Transactional
    public void updateProcessDate(Long id, String date){
        // 1) 문자열을 LocalDate 로 파싱
        LocalDate localDate = LocalDate.parse(date);
        // 2) 원하는 시각(예: 자정) 붙여서 LocalDateTime 으로 변환
        LocalDateTime processDate = localDate.atStartOfDay();
        userProfileRepository.updateUserProfileByProcessDate(processDate, id);
    }

    public UserProfileDTO getCurrentUserProfile(Long pid){
        UserProfileEntity userProfileEntity = userProfileRepository.findById(pid).orElseThrow(() -> new RuntimeException("UserProfile not found"));
        return toUserProfileDTO(userProfileEntity);
    }

    public UserProfileEntity toUserProfileEntity(CreateUserProfileDTO createUserProfileDTO, UserEntity userEntity, TimeLineEntity timeLineEntity) {
        return UserProfileEntity.builder()
                .user(userEntity)
                .timeLine(timeLineEntity)
                .nickname(createUserProfileDTO.getNickname())
                .cashBalance(timeLineEntity.getSeedMoney())
                .processDate(timeLineEntity.getFrom())
                .build();
    }

    public UserProfileDTO toUserProfileDTO(UserProfileEntity entity) {
        Double totalInvested = holdingsRepository.getHoldingsTotalPrice(entity.getUsersProfileId());
        Double cashBalance = entity.getCashBalance();
        Double totalAsset = totalInvested + cashBalance;

        return UserProfileDTO.builder()
                .id(entity.getUsersProfileId())
                // 총 투자 자산
                .totalInvested(formatUtil.changePriceFormatter(totalInvested))
                // 총 자산
                .totalAssets(formatUtil.changePriceFormatter(totalAsset))
                // 현금 자산
                .cashBalance(formatUtil.changePriceFormatter(entity.getCashBalance()))
                .nickname(entity.getNickname())
                .name(entity.getTimeLine().getName())
                .timelineId(entity.getTimeLine().getTimelineId())
                .timelineType(entity.getTimeLine().getType())
                .timelineFrom(entity.getTimeLine().getFrom())
                .timelineTo(entity.getTimeLine().getTo())
                .processDate(entity.getProcessDate().toLocalDate())
                .seedMoney(entity.getTimeLine().getSeedMoney())
                .build();
    }
}
