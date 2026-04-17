package com.intouch.IntouchApps.adminAccess;

import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@RefreshScope
public class RuntimeConfigController {

    private final RuntimeConfigService configService;
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String getProperty(@RequestParam String key,@RequestHeader("CONFIG_CODE") String configCode, Principal principal) {

        return configService.getProperty(key , configCode , principal);
    }

    @GetMapping("/configCode")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String sendCode(@RequestParam String configKey, Principal principal) throws MessagingException, AccountNotActivatedException {
        return configService.getConfigCode(configKey, principal);
    }
    @PostMapping("/deleteConfigCode")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String deleteConfigCode(@RequestHeader("CONFIG_CODE") String configCode, Principal principal) {
        return configService.deleteConfigCode(configCode, principal);
    }
    @GetMapping("/reload")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String reloadConfigs(@RequestHeader("CONFIG_CODE") String configCode, Principal principal, HttpServletRequest httpServletRequest) {
        configService.reloadConfigs(configCode, principal, httpServletRequest);
        return "Configs reloaded successfully!";
    }

//    @GetMapping("/all")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
//    public List<AppConfig> getAllConfigs(@RequestHeader("CONFIG_CODE") String configCode, Principal principal) {
//        return configService.getProperty("ALL") != null ? null : null; // you can return list if needed
//    }

    @PostMapping("/set")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public AppConfig addOrUpdateConfig(@RequestParam String key,
                                       @RequestParam String value,
                                       @RequestParam(defaultValue = "STRING") String type,
                                       @RequestParam(defaultValue = "1", required = false) String version,
                                       @RequestHeader("CONFIG_CODE") String configCode, Principal principal) {
        return configService.saveOrUpdate(key, value, type, version, configCode , principal);
    }

    @GetMapping("/encrypt")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String encrypt(@RequestParam String value,@RequestHeader("CONFIG_CODE") String configCode, Principal principal) {
        return configService.encryptValue(value, configCode , principal);
    }
}

