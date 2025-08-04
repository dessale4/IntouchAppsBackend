package com.intouch.IntouchApps.adminAccess;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("adminAccess")
public class ManageUsersController {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void addRole(){

    }
}
