package team.shdsesc.stocksimul.userprofile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.user.UserEntity;
import team.shdsesc.stocksimul.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserProfileService {
    private final TimeLineRepository timeLineRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

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
        return UserProfileDTO.builder()
                .id(entity.getUsersProfileId())
                .totalInvested(entity.getTimeLine().getSeedMoney())
                .totalAssets(entity.getTimeLine().getSeedMoney() - entity.getCashBalance())
                .cashBalance(entity.getCashBalance())
                .nickname(entity.getNickname())
                .name(entity.getTimeLine().getName())
                .processDate(entity.getProcessDate())
                .build();
    }
}
