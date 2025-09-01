package team.shdsesc.stocksimul.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiUserDetailsService implements UserDetailsService {
    private final ApiUserRepository apiUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ApiUser> result = apiUserRepository.findById(username);
        log.info("username:"+username);
        ApiUser apiUser = result.orElseThrow(()->new UsernameNotFoundException("Wrong ClientId or ClientSecret"));
        log.info("ApiUser:"+apiUser);
        ApiUserDTO dto = new ApiUserDTO(apiUser.getClient_id(), apiUser.getClient_secret(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        log.info("loadUserByUsername:"+dto);
        return dto;
    }
}
