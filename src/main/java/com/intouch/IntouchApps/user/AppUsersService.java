package com.intouch.IntouchApps.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUsersService {
    private final UserRepository userRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    public List<AccountDTO> getAppUsernames(){
        return userRepository.findAll().stream()
                .map((u->mapUserToAccountDTO(u)))
                .collect(Collectors.toList());
    }
    private AccountDTO mapUserToAccountDTO(User user){
    return AccountDTO.builder()
            .accountEmail(standardPBEStringEncryptor.decrypt(user.getEmail()))
            .accountUsername(user.getPublicUserName())
            .build();
    }
}

