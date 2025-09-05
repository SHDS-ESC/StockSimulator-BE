package team.shdsesc.stocksimul.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.auth.dto.ApiUserDTO;
import team.shdsesc.stocksimul.auth.dto.MemberRole;
import team.shdsesc.stocksimul.auth.dto.UserRegisterRequest;
import team.shdsesc.stocksimul.auth.entity.ApiUser;
import team.shdsesc.stocksimul.auth.repository.ApiUserRepository;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiUserDetailsService implements UserDetailsService {

    private final ApiUserRepository apiUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<ApiUser> result = apiUserRepository.findApiUserWithRolesByUserId(userId);
        log.info("username:{}", userId);
        ApiUser apiUser = result.orElseThrow(() -> new UsernameNotFoundException("Wrong ClientId or ClientSecret"));
        log.info("ApiUser:{}", apiUser);
        ApiUserDTO dto = ApiUser.toDto(apiUser);
        log.info("loadUserByUsername:{}", dto);
        return dto;
    }

    public ApiUserDTO saveApiUser(UserRegisterRequest request) {
        ApiUser apiUser = ApiUser.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber("010-1234-5678")
                .level(Integer.parseInt(request.getLevel()))
                .tickerList(request.getTickerList())
                .build();

        apiUser.addMemberRole(MemberRole.USER);
        apiUserRepository.save(apiUser);
        return ApiUser.toDto(apiUser);
    }
}
