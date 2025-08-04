package com.intouch.IntouchApps.auth;

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
    private UserDTO userDTO;

}
