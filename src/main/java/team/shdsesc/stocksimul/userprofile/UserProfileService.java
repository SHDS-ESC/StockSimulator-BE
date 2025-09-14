package team.shdsesc.stocksimul.userprofile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.redis.dao.RedisDAO;
import team.shdsesc.stocksimul.user.UserEntity;
import team.shdsesc.stocksimul.user.UserRepository;

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
                .map(userProfile -> new UserProfileDTO().toUserProfileEntity(userProfile))
                .toList();
    }

    @Transactional
    public UserProfileEntity createUserProfile(CreateUserProfileDTO createUserProfileDTO) {
        UserEntity user = userRepository.findUserWithRolesByUserId(createUserProfileDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        TimeLineEntity timeLine = timeLineRepository.findById(createUserProfileDTO.getTimelineId()).orElseThrow(() -> new RuntimeException("Timeline not found"));
        UserProfileEntity userProfileEntity = UserProfileEntity.toUserProfileEntity(createUserProfileDTO, user, timeLine);
        userProfileRepository.save(userProfileEntity);
        userProfileRepository.updateCurrentProfileState(userProfileEntity.getUserProfileId(), user.getUsersEmail());
        return userProfileEntity;
    }


    @Transactional
    public void updateUserProfile(UpdateUserProfileDTO updateUserProfileDTO) {
        userProfileRepository.updateCurrentProfileState(updateUserProfileDTO.getUserProfileId(),updateUserProfileDTO.getEmail());
    }
}
