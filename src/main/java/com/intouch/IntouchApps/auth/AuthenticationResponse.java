package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.user.AgePolicyResponse;
import com.intouch.IntouchApps.user.UserDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String jwtToken;
    private String jwtRefreshToken;
    private String tokenType;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
    private AgePolicyResponse agePolicy;
//    private AuthUserDto user;
//    private UserDTO user;
}
