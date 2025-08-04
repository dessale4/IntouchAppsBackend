package com.intouch.IntouchApps.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("appUsers")
public class AppUsersController {
    private final AppUsersService appUsersService;
    @GetMapping("/userNames")
    public List<AccountDTO> getUserNames(){
        return appUsersService.getAppUsernames();
    }
}
