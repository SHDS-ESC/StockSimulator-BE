package team.shdsesc.stocksimul.auth.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class TokenAuthenticationException extends AuthenticationException {

    private final String errorCode;
    private final String errorMessage;

    public TokenAuthenticationException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
} 