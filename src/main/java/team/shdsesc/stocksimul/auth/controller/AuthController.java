package team.shdsesc.stocksimul.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.shdsesc.stocksimul.auth.dto.ApiUserDTO;
import team.shdsesc.stocksimul.auth.dto.UserRegisterRequest;
import team.shdsesc.stocksimul.auth.service.ApiUserDetailsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final ApiUserDetailsService apiUserDetailsService;

    @PostMapping("/register")
    public ApiUserDTO register(@RequestBody UserRegisterRequest request) {
        return apiUserDetailsService.saveApiUser(request);
    }

}