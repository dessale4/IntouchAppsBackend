package com.intouch.IntouchApps.appkeys;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("keyFamily")
@RequiredArgsConstructor
public class KeyFamilyController {
    private final KeyFamilyService keyFamilyService;
    @PostMapping("/addKeyBasics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public KeyFamily addKeyBasics(@RequestBody AppKeyRequest AppKeyRequest) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return keyFamilyService.addKeyBasics(AppKeyRequest);
    }

    @GetMapping("/getKeyFamilies")
    public List<KeyFamily> getAllKeyFamilies(){
        return keyFamilyService.getAllKeyFamilies();
    }

    @GetMapping("/getKeyFamiliesWithDefaultExamples")
    public List<KeyFamilyResponse> getKeyFamiliesWithDefaultExamples(){

        return keyFamilyService.getKeyFamiliesWithDefaultExamples();
    }
    @GetMapping("/getKeyFamilyWithDefaultExamples")
    public KeyFamilyResponse getKeyFamilyWithDefaultExamples(@RequestParam("keyFamilyId") Integer keyFamilyId){
        return keyFamilyService.getKeyFamilyWithDefaultExamples(keyFamilyId);
    }
}
