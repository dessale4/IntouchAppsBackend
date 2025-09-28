package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultDTO;
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
    public KeyFamilyDefaultDTO addKeyBasics(@RequestBody AppKeyRequest AppKeyRequest) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return keyFamilyService.addKeyBasics(AppKeyRequest);
    }

    @GetMapping("/getKeyFamilies")
    public List<KeyFamily> getAllKeyFamilies(){
        return keyFamilyService.getAllKeyFamilies();
    }

//    @GetMapping("/getKeyFamiliesWithDefaultExamples")
//    public List<KeyFamilyResponse> getKeyFamiliesWithDefaultExamples(){
//
//        return keyFamilyService.getKeyFamiliesWithDefaultExamples();
//    }
@GetMapping("/getKeyFamiliesWithDefaultExamples")
public List<KeyFamilyDefaultDTO> getKeyFamiliesWithDefaultExamples(){

    return keyFamilyService.getKeyFamiliesWithDefaultExamples();
}
    @GetMapping("/getKeyFamilyWithDefaultExamples")
    public KeyFamilyDefaultDTO getKeyFamilyWithDefaultExamples(@RequestParam("keyFamilyId") Integer keyFamilyId){
        return keyFamilyService.getKeyFamilyWithDefaultExamples(keyFamilyId);
    }
}
