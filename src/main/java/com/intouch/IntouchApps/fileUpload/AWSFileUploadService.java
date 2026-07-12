//package com.intouch.IntouchApps.fileUpload;
//
//import com.intouch.IntouchApps.appkeys.*;
//import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultDTO;
//import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultMapper;
//import com.intouch.IntouchApps.handler.AWSFileUploadException;
//import com.intouch.IntouchApps.utils.AppObjectMapper;
//import org.springframework.transaction.annotation.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//import software.amazon.awssdk.utils.IoUtils;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URI;
//import java.util.Comparator;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@RefreshScope
//@Transactional
//public class AWSFileUploadService {
//    @Value("${application.aws.s3.access_key}")
//    private String awsS3AccessKey;
//    @Value("${application.aws.s3.secret_key}")
//    private String awsS3SecretKey;
//    @Value("${application.aws.s3.bucket_name}")
//    private String awsS3BucketName;
//    private final KeyFamilyDefaultMapper keyFamilyDefaultMapper;
//
//    private final S3Client s3Client;
//    private final KeyExampleRepository keyExampleRepository;
//    private final AppKeyRepository appKeyRepository;
//    private final KeyFamilyRepository keyFamilyRepository;
//    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
//    private final AppObjectMapper appObjectMapper;
//    private final KeyFamilyAudioRepository keyFamilyAudioRepository;
//    @CacheEvict(cacheNames = "defaultKeyFamilies", key = "'defaultKeyFamilies'")
//    public KeyExample uploadKeyExampleImage(MultipartFile file, Integer keyFamilyId, Integer keyId, Integer exampleId, String folderName){
//        KeyExample storedKeyExample = keyExampleRepository.findById(exampleId).orElseThrow(() -> new RuntimeException("No key example found with id " + exampleId));
//        if(storedKeyExample.getKeyId() !=keyId || storedKeyExample.getKeyFamilyId() != keyFamilyId){
//            throw new RuntimeException("Some thing went wrong");
//        }
//        String awsFileName = file.getOriginalFilename();
//        if(storedKeyExample.getImageFileName() != null){
//            throw new RuntimeException(awsFileName + " :Already exists in DB");
//        }
//        try {
//            String awsS3FileURl = saveFileToAWSS3Bucket(file, "image/jpg", folderName, true);
//
//            storedKeyExample.setImageFileName(awsFileName);
//            storedKeyExample.setImageUrl(awsS3FileURl);
//
//            storedKeyExample = keyExampleRepository.save(storedKeyExample);
//        } catch (Exception e) {
//           throw new RuntimeException(e.getMessage());
//        }
//        return storedKeyExample;
//    }
//    @CacheEvict(cacheNames = "defaultKeyFamilies", key = "'defaultKeyFamilies'")
//    public KeyExample uploadKeyExampleAudio(MultipartFile file, Integer keyFamilyId, Integer keyId, Integer exampleId, String folderName){
//
//        KeyExample storedKeyExample = keyExampleRepository.findById(exampleId).orElseThrow(() -> new RuntimeException("No key example found with id " + exampleId));
//
//        if(storedKeyExample.getKeyId() !=keyId || storedKeyExample.getKeyFamilyId() != keyFamilyId){
//            throw new RuntimeException("Some thing went wrong");
//        }
//        String awsFileName = file.getOriginalFilename();
////        if(storedKeyExample.getAudioFileName() != null){
////            throw new RuntimeException(awsFileName + " :Already exists in DB");
////        }
//        try {
//            String awsS3FileURl = saveFileToAWSS3Bucket(file, "video/mp4", folderName, true);
//
//            storedKeyExample.setAudioFileName(awsFileName);
//            storedKeyExample.setAudioUrl(awsS3FileURl);
//
//            storedKeyExample = keyExampleRepository.save(storedKeyExample);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }
//        return storedKeyExample;
//    }
//    @Transactional
//    @CacheEvict(
//            cacheNames = "defaultKeyFamilies",
//            key = "'defaultKeyFamilies'"
//    )
//    public AppKey uploadKeyAudio(
//            MultipartFile file,
//            Integer keyFamilyId,
//            Integer keyId,
//            String folderName,
//            boolean requestedAsDefault
//    ) {
//        AppKey storedAppKey = appKeyRepository
//                .findAppKeyByKeyFamilyIdAndKeyId(keyFamilyId, keyId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException("AppKey not found.")
//                );
//
//        String originalFileName = file.getOriginalFilename();
//
//        if (originalFileName == null || originalFileName.isBlank()) {
//            throw new IllegalArgumentException("Audio file name is required.");
//        }
//
//        String awsFileName = originalFileName.trim();
//
//        KeyAudio matchingExistingAudio = storedAppKey.getKeyAudios()
//                .stream()
//                .filter(audio ->
//                        audio.getKeyAudioFileName() != null &&
//                                audio.getKeyAudioFileName()
//                                        .equalsIgnoreCase(awsFileName)
//                )
//                .findFirst()
//                .orElse(null);
//
//        KeyAudio existingDefaultAudio = storedAppKey.getKeyAudios()
//                .stream()
//                .filter(KeyAudio::isDefault)
//                .max(Comparator.comparing(KeyAudio::getId))
//                .orElse(null);
//
//        boolean replacingCurrentDefault =
//                matchingExistingAudio != null &&
//                        matchingExistingAudio.isDefault();
//
//        boolean makeUploadedAudioDefault =
//                requestedAsDefault ||
//                        replacingCurrentDefault ||
//                        existingDefaultAudio == null;
//
//        try {
//            String contentType = file.getContentType();
//
//            if (contentType == null || contentType.isBlank()) {
//                contentType = "audio/mp4";
//            }
//
//            boolean replacingExistingAudio = matchingExistingAudio != null;
//
//            String awsS3FileUrl = saveFileToAWSS3Bucket(
//                    file,
//                    contentType,
//                    folderName,
//                    replacingExistingAudio
//            );
//
//            if (makeUploadedAudioDefault) {
//                storedAppKey.getKeyAudios()
//                        .stream()
//                        .filter(KeyAudio::isDefault)
//                        .forEach(audio -> audio.setDefault(false));
//            }
//
//            if (matchingExistingAudio != null) {
//                // Update the existing database record instead of inserting.
//                matchingExistingAudio.setKeyAudioUrl(awsS3FileUrl);
//                matchingExistingAudio.setKeyFamilyId(keyFamilyId);
//                matchingExistingAudio.setKeyId(keyId);
//                matchingExistingAudio.setKeyAudioFileName(awsFileName);
//                matchingExistingAudio.setDefault(makeUploadedAudioDefault);
//            } else {
//                KeyAudio newAudio = KeyAudio.builder()
//                        .keyAudioUrl(awsS3FileUrl)
//                        .keyId(keyId)
//                        .keyFamilyId(keyFamilyId)
//                        .keyAudioFileName(awsFileName)
//                        .isDefault(makeUploadedAudioDefault)
//                        .build();
//
//                storedAppKey.addKeyAudio(newAudio);
//            }
//
//            return appKeyRepository.save(storedAppKey);
//
//        } catch (Exception exception) {
//            throw new RuntimeException(
//                    "Unable to upload key audio: " + exception.getMessage(),
//                    exception
//            );
//        }
//    }
//    private void deleteFileFromAWSS3Bucket(String fileUrl) {
//        if (fileUrl == null || fileUrl.isBlank()) {
//            return;
//        }
//
//        try {
//            URI uri = URI.create(fileUrl);
//
//            // Remove the leading "/"
//            String objectKey = uri.getPath().startsWith("/")
//                    ? uri.getPath().substring(1)
//                    : uri.getPath();
//
//            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
//                    .bucket(awsS3BucketName)
//                    .key(objectKey)
//                    .build();
//
//            s3Client.deleteObject(deleteRequest);
//
//        } catch (Exception exception) {
//            throw new RuntimeException(
//                    "Unable to delete existing S3 object: " + fileUrl,
//                    exception
//            );
//        }
//    }
//    private String saveFileToAWSS3Bucket(
//            MultipartFile file,
//            String contentType,
//            String folderName,
//            boolean overwriteExisting
//    ) throws IOException, AWSFileUploadException {
//
//        String originalFileName = file.getOriginalFilename();
//
//        if (originalFileName == null || originalFileName.isBlank()) {
//            throw new IllegalArgumentException("File name is required.");
//        }
//
//        String s3FileName = originalFileName.trim();
//        String fileFolderLocation = folderName + "/" + s3FileName;
//
//        boolean fileAlreadyExists =
//                doesFileExistInTheSpecifiedLocation(fileFolderLocation);
//
//        if (fileAlreadyExists && !overwriteExisting) {
//            return buildS3FileUrl(fileFolderLocation);
//        }
//
//        /*
//         * S3 PutObject replaces the object automatically when the same
//         * bucket and object key are used.
//         */
//        uploadFile(file, fileFolderLocation, contentType);
//
//        return buildS3FileUrl(fileFolderLocation);
//    }
//
//    private String buildS3FileUrl(String objectKey) {
//        return "https://" +
//                awsS3BucketName +
//                ".s3.amazonaws.com/" +
//                objectKey;
//    }
//    @CacheEvict(cacheNames = "defaultKeyFamilies", key = "'defaultKeyFamilies'")
//    public String uploadFile(MultipartFile file, String fileFolderLocation, String contentType) throws IOException {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(awsS3BucketName)
//                .key(fileFolderLocation)
//                .contentType(contentType)
//                .build();
//        InputStream inputStream = null;
//        try{
//            inputStream = file.getInputStream();
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//            bufferedInputStream.mark(Integer.MAX_VALUE); // Mark at the beginning of the stream
//            PutObjectResponse putObjectResponse = s3Client.putObject(request, RequestBody.fromInputStream(bufferedInputStream, file.getSize()));
//            String awsFileLocation = "https://" + awsS3BucketName + ".s3.amazonaws.com/" + fileFolderLocation;
////            awsFileLocation = standardPBEStringEncryptor.encrypt(awsFileLocation);
//            return awsFileLocation;
//        } finally {
//            IoUtils.closeQuietly(inputStream, null);
//        }
//    }
//    private boolean doesFileExistInTheSpecifiedLocation(String fileFolderLocation){
//        try {
//            s3Client.getObject(GetObjectRequest.builder()
//                            .bucket(awsS3BucketName)
//                            .key(fileFolderLocation)
//                    .build());
//            return true; // File exists
//        } catch (NoSuchKeyException e) {
//            if (e.statusCode() == 404) {
//                return false; // File does not exist
//            } else {
//                e.printStackTrace();// Other error occurred
//                throw new RuntimeException(e.getMessage());
//            }
//        }catch (Exception e){// Other error occurred
//            e.printStackTrace();
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//    @CacheEvict(cacheNames = "defaultKeyFamilies", key = "'defaultKeyFamilies'")
//    public KeyFamilyDefaultDTO uploadKeyFamilyAudio(MultipartFile file, Integer keyFamilyId, String folderName) {
//        KeyFamily storedKeyFamily = keyFamilyRepository.findByKeyFamilyId(keyFamilyId).orElseThrow(() -> new RuntimeException("KeyFamily not found with keyFamilyId: " + keyFamilyId));
//        String keyFamilyFileName = file.getOriginalFilename();
//        KeyFamilyAudio storedKeyFamilyAudio = keyFamilyAudioRepository.findByKeyFamilyIdAndKeyFamilyAudioFileName(keyFamilyId, keyFamilyFileName);
//        if(storedKeyFamilyAudio != null){
//            throw new RuntimeException("KeyFamily Audio already exists with name: " + keyFamilyFileName);
//        }
////        KeyFamilyResponse keyFamilyResponse = null;
////        KeyFamilyAudio defaultKeyFamilyAudio = storedKeyFamily.getDefaultKeyFamilyAudio();
//                KeyFamilyAudio defaultKeyFamilyAudio = null;
//        Optional<KeyFamilyAudio> keyFamilyAudioOptional = storedKeyFamily.getKeyFamilyAudioSet().stream().filter(au -> au.isDefault()).findFirst();
//        if(keyFamilyAudioOptional.isPresent()){
//            defaultKeyFamilyAudio = keyFamilyAudioOptional.get();
//        }
//        KeyFamilyAudio keyFamilyAudio = KeyFamilyAudio.builder().build();
//        try{
//            String awsS3FileURl = saveFileToAWSS3Bucket(file, "video/mp4", folderName, true);
//
//            if(defaultKeyFamilyAudio == null){
//                keyFamilyAudio.setKeyFamilyId(keyFamilyId);
//                keyFamilyAudio.setDefault(true);
//                keyFamilyAudio.setKeyFamilyAudioUrl(awsS3FileURl);
//                keyFamilyAudio.setKeyFamilyAudioFileName(keyFamilyFileName);
//                storedKeyFamily.addKeyFamilyAudio(keyFamilyAudio);
////                storedKeyFamily.setDefaultKeyFamilyAudio(keyFamilyAudio);
//                storedKeyFamily = keyFamilyRepository.save(storedKeyFamily);
////                keyFamilyResponse = appObjectMapper.mapKeyFamilyToKeyFamilyResponse(storedKeyFamily);
//                return keyFamilyDefaultMapper.toKeyFamilyDefaultDTO(storedKeyFamily);
//            }else{
//                keyFamilyAudio.setKeyFamilyId(keyFamilyId);
//                keyFamilyAudio.setKeyFamilyAudioUrl(awsS3FileURl);
//                keyFamilyAudio.setKeyFamilyAudioFileName(keyFamilyFileName);
//                storedKeyFamily.addKeyFamilyAudio(keyFamilyAudio);
//                storedKeyFamily = keyFamilyRepository.save(storedKeyFamily);
////                keyFamilyResponse = appObjectMapper.mapKeyFamilyToKeyFamilyResponse(storedKeyFamily);
//                return keyFamilyDefaultMapper.toKeyFamilyDefaultDTO(storedKeyFamily);
//            }
//        }catch (Exception e){
//            throw new RuntimeException(e.getMessage());
//        }
////        return keyFamilyResponse;
//    }
//}

package com.intouch.IntouchApps.fileUpload;

import com.intouch.IntouchApps.appkeys.AppKey;
import com.intouch.IntouchApps.appkeys.AppKeyRepository;
import com.intouch.IntouchApps.appkeys.KeyAudio;
import com.intouch.IntouchApps.appkeys.KeyExample;
import com.intouch.IntouchApps.appkeys.KeyExampleRepository;
import com.intouch.IntouchApps.appkeys.KeyFamily;
import com.intouch.IntouchApps.appkeys.KeyFamilyAudio;
import com.intouch.IntouchApps.appkeys.KeyFamilyAudioRepository;
import com.intouch.IntouchApps.appkeys.KeyFamilyRepository;
import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultDTO;
import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultMapper;
import com.intouch.IntouchApps.handler.AWSFileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@RefreshScope
@Transactional
public class AWSFileUploadService {

    @Value("${application.aws.s3.bucket_name}")
    private String awsS3BucketName;

    private final S3Client s3Client;
    private final KeyExampleRepository keyExampleRepository;
    private final AppKeyRepository appKeyRepository;
    private final KeyFamilyRepository keyFamilyRepository;
    private final KeyFamilyAudioRepository keyFamilyAudioRepository;
    private final KeyFamilyDefaultMapper keyFamilyDefaultMapper;

    /*
     * ============================================================
     * Key example image
     * ============================================================
     */

    @CacheEvict(
            cacheNames = "defaultKeyFamilies",
            key = "'defaultKeyFamilies'"
    )
    public KeyExample uploadKeyExampleImage(
            MultipartFile file,
            Integer keyFamilyId,
            Integer keyId,
            Integer exampleId,
            String folderName
    ) {
        validateFile(file);

        KeyExample storedKeyExample = keyExampleRepository
                .findById(exampleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No key example found with id: " + exampleId
                        )
                );

        ensureExampleBelongsToKey(
                storedKeyExample,
                keyFamilyId,
                keyId
        );

        String fileName = requireFileName(file);
        String oldFileUrl = storedKeyExample.getImageUrl();

        try {
            String contentType = resolveImageContentType(file, fileName);

            /*
             * putObject overwrites an existing S3 object when the object key
             * is the same.
             */
            String newFileUrl = uploadFileToS3(
                    file,
                    folderName,
                    fileName,
                    contentType
            );

            storedKeyExample.setImageFileName(fileName);
            storedKeyExample.setImageUrl(newFileUrl);

            KeyExample savedExample =
                    keyExampleRepository.saveAndFlush(storedKeyExample);

            deletePreviousObjectWhenLocationChanged(
                    oldFileUrl,
                    newFileUrl
            );

            return savedExample;

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Unable to upload key example image: " +
                            exception.getMessage(),
                    exception
            );
        }
    }

    /*
     * ============================================================
     * Key example audio
     * ============================================================
     */

    @CacheEvict(
            cacheNames = "defaultKeyFamilies",
            key = "'defaultKeyFamilies'"
    )
    public KeyExample uploadKeyExampleAudio(
            MultipartFile file,
            Integer keyFamilyId,
            Integer keyId,
            Integer exampleId,
            String folderName
    ) {
        validateFile(file);

        KeyExample storedKeyExample = keyExampleRepository
                .findById(exampleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No key example found with id: " + exampleId
                        )
                );

        ensureExampleBelongsToKey(
                storedKeyExample,
                keyFamilyId,
                keyId
        );

        String fileName = requireFileName(file);
        String oldFileUrl = storedKeyExample.getAudioUrl();

        try {
            String contentType = resolveAudioContentType(file, fileName);

            String newFileUrl = uploadFileToS3(
                    file,
                    folderName,
                    fileName,
                    contentType
            );

            storedKeyExample.setAudioFileName(fileName);
            storedKeyExample.setAudioUrl(newFileUrl);

            KeyExample savedExample =
                    keyExampleRepository.saveAndFlush(storedKeyExample);

            deletePreviousObjectWhenLocationChanged(
                    oldFileUrl,
                    newFileUrl
            );

            return savedExample;

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Unable to upload key example audio: " +
                            exception.getMessage(),
                    exception
            );
        }
    }

    /*
     * ============================================================
     * Individual key audio
     * ============================================================
     */

    @CacheEvict(
            cacheNames = "defaultKeyFamilies",
            key = "'defaultKeyFamilies'"
    )
    public AppKey uploadKeyAudio(
            MultipartFile file,
            Integer keyFamilyId,
            Integer keyId,
            String folderName,
            boolean requestedAsDefault
    ) {
        validateFile(file);

        AppKey storedAppKey = appKeyRepository
                .findAppKeyByKeyFamilyIdAndKeyId(
                        keyFamilyId,
                        keyId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "AppKey not found for key family " +
                                        keyFamilyId +
                                        " and key " +
                                        keyId
                        )
                );

        String fileName = requireFileName(file);

        /*
         * Normalize accidental existing duplicate defaults first.
         * The newest persisted default remains selected.
         */
        KeyAudio existingDefaultAudio =
                normalizeAndGetDefaultKeyAudio(
                        storedAppKey.getKeyAudios()
                );

        KeyAudio matchingExistingAudio =
                storedAppKey.getKeyAudios()
                        .stream()
                        .filter(audio ->
                                audio.getKeyAudioFileName() != null &&
                                        audio.getKeyAudioFileName()
                                                .equalsIgnoreCase(fileName)
                        )
                        .findFirst()
                        .orElse(null);

        boolean replacingCurrentDefault =
                matchingExistingAudio != null &&
                        matchingExistingAudio.isDefault();

        boolean makeUploadedAudioDefault =
                requestedAsDefault ||
                        replacingCurrentDefault ||
                        existingDefaultAudio == null;

        try {
            String contentType =
                    resolveAudioContentType(file, fileName);

            /*
             * Always upload the supplied key-audio file.
             *
             * If the same folder and filename already exist, S3 overwrites
             * that object. We must not skip the upload just because the key
             * already exists.
             */
            String newFileUrl = uploadFileToS3(
                    file,
                    folderName,
                    fileName,
                    contentType
            );

            if (makeUploadedAudioDefault) {
                storedAppKey.getKeyAudios()
                        .forEach(audio -> audio.setDefault(false));
            }

            if (matchingExistingAudio != null) {
                /*
                 * Same filename: update the existing database row instead of
                 * inserting another row and violating the filename constraint.
                 */
                matchingExistingAudio.setKeyFamilyId(keyFamilyId);
                matchingExistingAudio.setKeyId(keyId);
                matchingExistingAudio.setKeyAudioFileName(fileName);
                matchingExistingAudio.setKeyAudioUrl(newFileUrl);
                matchingExistingAudio.setDefault(
                        makeUploadedAudioDefault
                );
            } else {
                KeyAudio newAudio = KeyAudio.builder()
                        .keyFamilyId(keyFamilyId)
                        .keyId(keyId)
                        .keyAudioFileName(fileName)
                        .keyAudioUrl(newFileUrl)
                        .isDefault(makeUploadedAudioDefault)
                        .build();

                storedAppKey.addKeyAudio(newAudio);
            }

            return appKeyRepository.saveAndFlush(storedAppKey);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Unable to upload key audio: " +
                            exception.getMessage(),
                    exception
            );
        }
    }

    /*
     * ============================================================
     * Key-family audio
     * ============================================================
     */

    /**
     * Backward-compatible overload.
     *
     * Existing callers that do not yet send isDefault will continue working.
     */


    @CacheEvict(
            cacheNames = "defaultKeyFamilies",
            key = "'defaultKeyFamilies'"
    )
    public KeyFamilyDefaultDTO uploadKeyFamilyAudio(
            MultipartFile file,
            Integer keyFamilyId,
            String folderName,
            boolean requestedAsDefault
    ) {
        validateFile(file);

        KeyFamily storedKeyFamily = keyFamilyRepository
                .findByKeyFamilyId(keyFamilyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "KeyFamily not found with keyFamilyId: " +
                                        keyFamilyId
                        )
                );

        String fileName = requireFileName(file);

        KeyFamilyAudio existingDefaultAudio =
                normalizeAndGetDefaultKeyFamilyAudio(
                        storedKeyFamily.getKeyFamilyAudioSet()
                );

        KeyFamilyAudio matchingExistingAudio =
                storedKeyFamily.getKeyFamilyAudioSet()
                        .stream()
                        .filter(audio ->
                                audio.getKeyFamilyAudioFileName() != null &&
                                        audio.getKeyFamilyAudioFileName()
                                                .equalsIgnoreCase(fileName)
                        )
                        .findFirst()
                        .orElse(null);

        boolean replacingCurrentDefault =
                matchingExistingAudio != null &&
                        matchingExistingAudio.isDefault();

        boolean makeUploadedAudioDefault =
                requestedAsDefault ||
                        replacingCurrentDefault ||
                        existingDefaultAudio == null;

        try {
            String contentType =
                    resolveAudioContentType(file, fileName);

            String newFileUrl = uploadFileToS3(
                    file,
                    folderName,
                    fileName,
                    contentType
            );

            if (makeUploadedAudioDefault) {
                storedKeyFamily.getKeyFamilyAudioSet()
                        .forEach(audio -> audio.setDefault(false));
            }

            if (matchingExistingAudio != null) {
                /*
                 * Update the same database row when the filename already
                 * exists.
                 */
                matchingExistingAudio.setKeyFamilyId(keyFamilyId);
                matchingExistingAudio.setKeyFamilyAudioFileName(fileName);
                matchingExistingAudio.setKeyFamilyAudioUrl(newFileUrl);
                matchingExistingAudio.setDefault(
                        makeUploadedAudioDefault
                );
            } else {
                KeyFamilyAudio newAudio = KeyFamilyAudio.builder()
                        .keyFamilyId(keyFamilyId)
                        .keyFamilyAudioFileName(fileName)
                        .keyFamilyAudioUrl(newFileUrl)
                        .isDefault(makeUploadedAudioDefault)
                        .build();

                storedKeyFamily.addKeyFamilyAudio(newAudio);
            }

            KeyFamily savedKeyFamily =
                    keyFamilyRepository.saveAndFlush(storedKeyFamily);

            return keyFamilyDefaultMapper
                    .toKeyFamilyDefaultDTO(savedKeyFamily);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Unable to upload key-family audio: " +
                            exception.getMessage(),
                    exception
            );
        }
    }

    /*
     * ============================================================
     * Default normalization
     * ============================================================
     */

    private KeyAudio normalizeAndGetDefaultKeyAudio(
            Set<KeyAudio> keyAudios
    ) {
        if (keyAudios == null || keyAudios.isEmpty()) {
            return null;
        }

        KeyAudio selectedDefault = keyAudios
                .stream()
                .filter(KeyAudio::isDefault)
                .max(
                        Comparator.comparing(
                                audio -> audio.getId() == null
                                        ? 0
                                        : audio.getId()
                        )
                )
                .orElse(null);

        if (selectedDefault != null) {
            keyAudios.stream()
                    .filter(KeyAudio::isDefault)
                    .filter(audio ->
                            !Objects.equals(
                                    audio.getId(),
                                    selectedDefault.getId()
                            )
                    )
                    .forEach(audio -> audio.setDefault(false));
        }

        return selectedDefault;
    }

    private KeyFamilyAudio normalizeAndGetDefaultKeyFamilyAudio(
            Set<KeyFamilyAudio> familyAudios
    ) {
        if (familyAudios == null || familyAudios.isEmpty()) {
            return null;
        }

        KeyFamilyAudio selectedDefault = familyAudios
                .stream()
                .filter(KeyFamilyAudio::isDefault)
                .max(
                        Comparator.comparing(
                                audio -> audio.getId() == null
                                        ? 0
                                        : audio.getId()
                        )
                )
                .orElse(null);

        if (selectedDefault != null) {
            familyAudios.stream()
                    .filter(KeyFamilyAudio::isDefault)
                    .filter(audio ->
                            !Objects.equals(
                                    audio.getId(),
                                    selectedDefault.getId()
                            )
                    )
                    .forEach(audio -> audio.setDefault(false));
        }

        return selectedDefault;
    }

    /*
     * ============================================================
     * Validation
     * ============================================================
     */

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a file to upload."
            );
        }
    }

    private String requireFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File name is required."
            );
        }

        /*
         * Remove any browser-supplied client path.
         */
        String normalizedFileName =
                originalFileName.replace("\\", "/");

        int lastSlashIndex =
                normalizedFileName.lastIndexOf('/');

        if (lastSlashIndex >= 0) {
            normalizedFileName =
                    normalizedFileName.substring(
                            lastSlashIndex + 1
                    );
        }

        normalizedFileName = normalizedFileName.trim();

        if (normalizedFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File name is required."
            );
        }

        return normalizedFileName;
    }

    private void ensureExampleBelongsToKey(
            KeyExample keyExample,
            Integer keyFamilyId,
            Integer keyId
    ) {
        if (!Objects.equals(
                keyExample.getKeyFamilyId(),
                keyFamilyId
        ) || !Objects.equals(
                keyExample.getKeyId(),
                keyId
        )) {
            throw new IllegalArgumentException(
                    "The selected example does not belong to " +
                            "the supplied key."
            );
        }
    }

    /*
     * ============================================================
     * Content types
     * ============================================================
     */

    private String resolveAudioContentType(
            MultipartFile file,
            String fileName
    ) {
        String suppliedContentType =
                file.getContentType();

        if (suppliedContentType != null &&
                suppliedContentType
                        .toLowerCase()
                        .startsWith("audio/")) {
            return suppliedContentType;
        }

        String normalizedFileName =
                fileName.toLowerCase();

        if (normalizedFileName.endsWith(".m4a")) {
            return "audio/mp4";
        }

        if (normalizedFileName.endsWith(".mp3")) {
            return "audio/mpeg";
        }

        if (normalizedFileName.endsWith(".wav")) {
            return "audio/wav";
        }

        if (normalizedFileName.endsWith(".ogg")) {
            return "audio/ogg";
        }

        if (normalizedFileName.endsWith(".aac")) {
            return "audio/aac";
        }

        return "application/octet-stream";
    }

    private String resolveImageContentType(
            MultipartFile file,
            String fileName
    ) {
        String suppliedContentType =
                file.getContentType();

        if (suppliedContentType != null &&
                suppliedContentType
                        .toLowerCase()
                        .startsWith("image/")) {
            return suppliedContentType;
        }

        String normalizedFileName =
                fileName.toLowerCase();

        if (normalizedFileName.endsWith(".png")) {
            return "image/png";
        }

        if (normalizedFileName.endsWith(".webp")) {
            return "image/webp";
        }

        if (normalizedFileName.endsWith(".gif")) {
            return "image/gif";
        }

        return "image/jpeg";
    }

    /*
     * ============================================================
     * S3 operations
     * ============================================================
     */

    private String uploadFileToS3(
            MultipartFile file,
            String folderName,
            String fileName,
            String contentType
    ) throws IOException {

        String objectKey = buildObjectKey(folderName, fileName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsS3BucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        byte[] fileBytes = file.getBytes();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(fileBytes)
        );

        return buildS3FileUrl(objectKey);
    }

    private String buildObjectKey(
            String folderName,
            String fileName
    ) {
        if (folderName == null ||
                folderName.isBlank()) {
            return fileName;
        }

        String normalizedFolder =
                folderName.trim()
                        .replace("\\", "/")
                        .replaceAll("^/+", "")
                        .replaceAll("/+$", "");

        return normalizedFolder + "/" + fileName;
    }

    private String buildS3FileUrl(String objectKey) {
        return "https://" +
                awsS3BucketName +
                ".s3.amazonaws.com/" +
                objectKey;
    }

    /*
     * If a new upload uses a different filename or folder, clean up the
     * previous S3 object after the database update succeeds.
     *
     * Cleanup failure does not fail the user upload.
     */
    private void deletePreviousObjectWhenLocationChanged(
            String previousUrl,
            String newUrl
    ) {
        if (previousUrl == null ||
                previousUrl.isBlank() ||
                Objects.equals(previousUrl, newUrl)) {
            return;
        }

        try {
            deleteFileFromS3ByUrl(previousUrl);
        } catch (Exception exception) {
            log.warn(
                    "New file was saved, but previous S3 object " +
                            "could not be deleted: {}",
                    previousUrl,
                    exception
            );
        }
    }

    private void deleteFileFromS3ByUrl(String fileUrl) {
        if (fileUrl == null ||
                fileUrl.isBlank()) {
            return;
        }

        URI uri = URI.create(fileUrl);

        String rawPath = uri.getRawPath();

        if (rawPath == null ||
                rawPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Unable to determine S3 object key from URL: " +
                            fileUrl
            );
        }

        String encodedObjectKey =
                rawPath.startsWith("/")
                        ? rawPath.substring(1)
                        : rawPath;

        String objectKey = URLDecoder.decode(
                encodedObjectKey,
                StandardCharsets.UTF_8
        );

        DeleteObjectRequest deleteRequest =
                DeleteObjectRequest.builder()
                        .bucket(awsS3BucketName)
                        .key(objectKey)
                        .build();

        s3Client.deleteObject(deleteRequest);
    }
}