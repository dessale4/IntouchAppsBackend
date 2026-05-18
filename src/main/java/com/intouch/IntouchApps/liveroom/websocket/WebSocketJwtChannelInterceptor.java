package com.intouch.IntouchApps.liveroom.websocket;

import com.intouch.IntouchApps.constants.ClientType;
import com.intouch.IntouchApps.enums.JwtTokenType;
import com.intouch.IntouchApps.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketJwtChannelInterceptor implements ChannelInterceptor {

    private static final String EXPECTED_CLIENT_TYPE = ClientType.WEB_CLIENT;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          String clientType = accessor.getFirstNativeHeader("CLIENT_TYPE");

            if (!EXPECTED_CLIENT_TYPE.equals(clientType)) {
                throw new AccessDeniedException("Invalid WebSocket client type.");
            }

            String authorizationHeader =
                    accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader == null ||
                    !authorizationHeader.startsWith("Bearer ")) {
                throw new AccessDeniedException("Missing WebSocket Authorization header.");
            }

            String token = authorizationHeader.substring(7);
            String username = jwtService.extractUsername(token, JwtTokenType.ACCESS_TOKEN);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(token, userDetails, JwtTokenType.ACCESS_TOKEN)) {
                throw new AccessDeniedException("Invalid WebSocket token.");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            accessor.setUser(authentication);
        }
        return message;
    }
}