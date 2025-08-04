package com.intouch.IntouchApps.appkeys;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("keyFamilyAudio")
public class KeyFamilyAudioController {
    private final KeyFamilyAudioService keyFamilyAudioService;
    @PostMapping("/setTimeStamps")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public KeyFamilyAudio setKeyFamilyAudioTimeStamps(@RequestBody KeyFamilyAudioTimeStamp keyFamilyAudioTimeStamp, @RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyFamilyAudioId") Integer keyFamilyAudioId){
       return keyFamilyAudioService.setKeFamilyAudioTimeStamps(keyFamilyAudioTimeStamp, keyFamilyId,keyFamilyAudioId);
    }

    @PostMapping("/setKeyFamilyAudioAsDefault")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public KeyFamilyAudio setKeyFamilyAudioAsDefault(@RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyFamilyAudioId") Integer keyFamilyAudioId){
        return keyFamilyAudioService.setKeyFamilyAudioAsDefault(keyFamilyId,keyFamilyAudioId);
    }
    @GetMapping("/getKeyFamilyAudios")
    public Set<KeyFamilyAudio> getKeyFamilyAudios(@RequestParam("keyFamilyId") Integer keyFamilyId){
        return keyFamilyAudioService.getKeyFamilyAudios(keyFamilyId);
    }
}
