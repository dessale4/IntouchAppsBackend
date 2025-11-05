package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("keyExample")
public class KeyExampleController {
    private final KeyExampleService keyExampleService;
    @PostMapping("/addKeyExample")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public AppKey addKeyExample(@RequestBody KeyExample keyExample){
        return keyExampleService.addKeyExample(keyExample);
    }
    @PatchMapping("/editKeyExample")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public KeyFamilyDefaultDTO editKeyExample(@RequestParam Integer keyFamilyId, @RequestParam Integer keyExampleId, @RequestBody  Map<String, Object> keyExample){
        return keyExampleService.editKeyExample(keyFamilyId, keyExampleId, keyExample);
    }
    @PostMapping("/setAsDefault")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public AppKey setAsDefaultExample(@RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyId") Integer keyId, @RequestParam("keyExampleId") Integer keyExampleId){
        return keyExampleService.setAsDefaultExample(keyFamilyId, keyId, keyExampleId);
    }
    @GetMapping("/keyExamplesOfAKey")
    public List<KeyExample> getKeyExamplesForAKey(@RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyId")Integer keyId){
        return keyExampleService.getKeyExamplesForAKey(keyFamilyId, keyId);
    }
}
