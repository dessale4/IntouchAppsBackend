//package com.intouch.IntouchApps.liveroom.websocket;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.simp.SimpMessageType;
//import org.springframework.security.authorization.AuthorizationManager;
//import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
//import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
//
//@Configuration
////@EnableWebSocketSecurity
//public class WebSocketSecurityConfig {
//
//    @Bean
//    AuthorizationManager<Message<?>> messageAuthorizationManager(
//            MessageMatcherDelegatingAuthorizationManager.Builder messages
//    ) {
//        messages
//                .simpDestMatchers("/topic/intouch-rooms/**")
//                .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LIVEROOM_OWNER")
//
//                .simpTypeMatchers(
//                        SimpMessageType.CONNECT,
//                        SimpMessageType.DISCONNECT,
//                        SimpMessageType.HEARTBEAT
//                )
//                .permitAll()
//
//                .anyMessage()
//                .authenticated();
//
//        return messages.build();
//    }
//}