package com.intouch.IntouchApps.fileUpload;

import com.intouch.IntouchApps.appkeys.AppKey;
import com.intouch.IntouchApps.appkeys.KeyExample;
import com.intouch.IntouchApps.appkeys.KeyFamilyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("awsFileUpload")
@RequiredArgsConstructor
public class AWSFileUploadController {
    private final AWSFileUploadService awsFileUploadService;

    @PostMapping("/keyFamilyAudio")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public KeyFamilyResponse uploadKeyFamilyAudio(@RequestParam("file") MultipartFile file, @RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("folderName") String folderName) {

        return awsFileUploadService.uploadKeyFamilyAudio(file, keyFamilyId, folderName);
    }
    @PostMapping("/keyAudio")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public String keyAudioFileUpload(@RequestBody MultipartFile file, @RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyId") Integer keyId, @RequestParam("folderName") String folderName) {
        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }
        String message = "File uploaded successfully: ";

        AppKey AppKey = awsFileUploadService.uploadKeyAudio(file, keyFamilyId, keyId, folderName);

        return message;
    }
    @PostMapping("/keyExampleImage")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public String keyExampleImageUpload(@RequestParam MultipartFile file, @RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyId") Integer keyId, @RequestParam("exampleId") Integer exampleId, @RequestParam("folderName") String folderName) {
        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }

        String message = "Image File uploaded successfully: ";

        KeyExample keyExampleWithFileUpload = awsFileUploadService.uploadKeyExampleImage(file, keyFamilyId, keyId, exampleId, folderName);

        return message;
    }

    @PostMapping("/keyExampleAudio")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public String keyExampleAudioUpload(@RequestParam("file") MultipartFile file, @RequestParam("keyFamilyId") Integer keyFamilyId, @RequestParam("keyId") Integer keyId, @RequestParam("exampleId") Integer exampleId, @RequestParam("folderName") String folderName) {

        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }

        String message = "Audio File uploaded successfully: ";

        KeyExample keyExampleWithFileUpload = awsFileUploadService.uploadKeyExampleAudio(file, keyFamilyId, keyId, exampleId, folderName);

        return message;
    }

}
