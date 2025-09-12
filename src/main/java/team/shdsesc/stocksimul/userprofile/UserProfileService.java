package team.shdsesc.stocksimul.userprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.redis.dao.RedisDAO;
import team.shdsesc.stocksimul.user.UserEntity;
import team.shdsesc.stocksimul.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final TimeLineRepository timeLineRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ResponseEntity<List<TimeLineEntity>> getTimeLineList() {
        return ResponseEntity
                .ok()
                .body(timeLineRepository.findAll());
    }

    public ResponseEntity<UserProfileEntity> createUserProfile(CreateUserProfileDTO createUserProfileDTO) {
        UserEntity user = userRepository.findUserWithRolesByUserId(createUserProfileDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        TimeLineEntity timeLine = timeLineRepository.findById(createUserProfileDTO.getTimelineId()).orElseThrow(() -> new RuntimeException("Timeline not found"));
        UserProfileEntity userProfileEntity = UserProfileEntity.toUserProfileEntity(createUserProfileDTO, user, timeLine);
        userProfileRepository.save(userProfileEntity);

        return ResponseEntity
                .ok()
                .body(userProfileEntity);
    }

    public ResponseEntity<List<UserProfileDTO>> getUserProfileList(String email) {
        return ResponseEntity
                .ok()
                .body(userProfileRepository.findAll()
                        .stream()
                        .map(userProfile -> new UserProfileDTO().toUserProfileEntity(userProfile, email))
                        .toList());
    }
}
