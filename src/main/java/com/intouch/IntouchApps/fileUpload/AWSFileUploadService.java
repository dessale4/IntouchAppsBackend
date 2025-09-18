package com.intouch.IntouchApps.fileUpload;

import com.intouch.IntouchApps.appkeys.*;
import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultDTO;
import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultMapper;
import com.intouch.IntouchApps.handler.AWSFileUploadException;
import com.intouch.IntouchApps.utils.AppObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@RefreshScope
@Transactional
public class AWSFileUploadService {
    @Value("${application.aws.s3.access_key}")
    private String awsS3AccessKey;
    @Value("${application.aws.s3.secret_key}")
    private String awsS3SecretKey;
    @Value("${application.aws.s3.bucket_name}")
    private String awsS3BucketName;
    private final KeyFamilyDefaultMapper keyFamilyDefaultMapper;

    private final S3Client s3Client;
    private final KeyExampleRepository keyExampleRepository;
    private final AppKeyRepository appKeyRepository;
    private final KeyFamilyRepository keyFamilyRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private final AppObjectMapper appObjectMapper;
    private final KeyFamilyAudioRepository keyFamilyAudioRepository;
    public KeyExample uploadKeyExampleImage(MultipartFile file, Integer keyFamilyId, Integer keyId, Integer exampleId, String folderName){
        KeyExample storedKeyExample = keyExampleRepository.findById(exampleId).orElseThrow(() -> new RuntimeException("No key example found with id " + exampleId));
        if(storedKeyExample.getKeyId() !=keyId || storedKeyExample.getKeyFamilyId() != keyFamilyId){
            throw new RuntimeException("Some thing went wrong");
        }
        String awsFileName = file.getOriginalFilename();
        if(storedKeyExample.getImageFileName() != null){
            throw new RuntimeException(awsFileName + " :Already exists in DB");
        }
        try {
            String awsS3FileURl = saveFileToAWSS3Bucket(file, "image/jpg", folderName);

            storedKeyExample.setImageFileName(awsFileName);
            storedKeyExample.setImageUrl(awsS3FileURl);

            storedKeyExample = keyExampleRepository.save(storedKeyExample);
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage());
        }
        return storedKeyExample;
    }
    public KeyExample uploadKeyExampleAudio(MultipartFile file, Integer keyFamilyId, Integer keyId, Integer exampleId, String folderName){

        KeyExample storedKeyExample = keyExampleRepository.findById(exampleId).orElseThrow(() -> new RuntimeException("No key example found with id " + exampleId));

        if(storedKeyExample.getKeyId() !=keyId || storedKeyExample.getKeyFamilyId() != keyFamilyId){
            throw new RuntimeException("Some thing went wrong");
        }
        String awsFileName = file.getOriginalFilename();
//        if(storedKeyExample.getAudioFileName() != null){
//            throw new RuntimeException(awsFileName + " :Already exists in DB");
//        }
        try {
            String awsS3FileURl = saveFileToAWSS3Bucket(file, "video/mp4", folderName);

            storedKeyExample.setAudioFileName(awsFileName);
            storedKeyExample.setAudioUrl(awsS3FileURl);

            storedKeyExample = keyExampleRepository.save(storedKeyExample);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return storedKeyExample;
    }
    public AppKey uploadKeyAudio(MultipartFile file, Integer keyFamilyId, Integer keyId, String folderName) {
        AppKey storedAppKey = appKeyRepository.findAppKeyByKeyFamilyIdAndKeyId(keyFamilyId, keyId).orElseThrow(() -> new RuntimeException("AppKey not found"));
        String awsFileName = file.getOriginalFilename();
        boolean audioExists = storedAppKey.getKeyAudios()
                .stream()
                .filter(au -> au.getKeyAudioFileName().equals(awsFileName))
                .findFirst()
                .isPresent();
        if(audioExists){
            throw new RuntimeException("Table already has Key Audio with name: " + awsFileName);
        }
        try {
            String awsS3FileURl = saveFileToAWSS3Bucket(file, "video/mp4", folderName);
            Optional<KeyAudio> existingDefaultKeyAudio = storedAppKey.getKeyAudios().stream().filter(a -> a.isDefault()).findFirst();
            KeyAudio keyAudio = KeyAudio.builder()
                    .keyAudioUrl(awsS3FileURl)
                    .keyId(keyId)
                    .keyFamilyId(keyFamilyId)
                    .keyAudioFileName(awsFileName)
                    .isDefault(existingDefaultKeyAudio.isPresent() ? false : true)
                    .build();
            storedAppKey.addKeyAudio(keyAudio);
            if(keyAudio.isDefault() && existingDefaultKeyAudio.isPresent()){
                KeyAudio keyAudioToUpdate = existingDefaultKeyAudio.get();
                keyAudioToUpdate.setDefault(false);
                storedAppKey.addKeyAudio(keyAudioToUpdate);//will update the existing keyAudio
//                storedAppKey.setDefaultKeyAudio(keyAudio);

            }
            storedAppKey = appKeyRepository.save(storedAppKey);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return storedAppKey;
    }
    private String saveFileToAWSS3Bucket(MultipartFile file, String contentType, String folderName ) throws IOException, AWSFileUploadException {
        String fileName = file.getOriginalFilename();
//        int lastIndexOfDot = fileName.lastIndexOf(".");
        String s3FileName = fileName;
//        if (lastIndexOfDot > 0) {
//            s3FileName = fileName.substring(0, lastIndexOfDot);
//        } else {
//            s3FileName = fileName;
//        }
        String fileFolderLocation = folderName + "/" + s3FileName;

        boolean fileAlreadyExist = doesFileExistInTheSpecifiedLocation(fileFolderLocation);
        if (fileAlreadyExist) {
            String awsFileLocation = "https://" + awsS3BucketName + ".s3.amazonaws.com/" + fileFolderLocation;
//            awsFileLocation = standardPBEStringEncryptor.encrypt(awsFileLocation);
            return awsFileLocation;
//            throw new AWSFileUploadException("No need to save the same file multiple times.");
        }

        String uploadedFile = uploadFile(file, fileFolderLocation, contentType);

        return uploadedFile.toString();
    }
    public String uploadFile(MultipartFile file, String fileFolderLocation, String contentType) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsS3BucketName)
                .key(fileFolderLocation)
                .contentType(contentType)
                .build();
        InputStream inputStream = null;
        try{
            inputStream = file.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedInputStream.mark(Integer.MAX_VALUE); // Mark at the beginning of the stream
            PutObjectResponse putObjectResponse = s3Client.putObject(request, RequestBody.fromInputStream(bufferedInputStream, file.getSize()));
            String awsFileLocation = "https://" + awsS3BucketName + ".s3.amazonaws.com/" + fileFolderLocation;
//            awsFileLocation = standardPBEStringEncryptor.encrypt(awsFileLocation);
            return awsFileLocation;
        } finally {
            IoUtils.closeQuietly(inputStream, null);
        }
    }
    private boolean doesFileExistInTheSpecifiedLocation(String fileFolderLocation){
        try {
            s3Client.getObject(GetObjectRequest.builder()
                            .bucket(awsS3BucketName)
                            .key(fileFolderLocation)
                    .build());
            return true; // File exists
        } catch (NoSuchKeyException e) {
            if (e.statusCode() == 404) {
                return false; // File does not exist
            } else {
                e.printStackTrace();// Other error occurred
                throw new RuntimeException(e.getMessage());
            }
        }catch (Exception e){// Other error occurred
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    public KeyFamilyDefaultDTO uploadKeyFamilyAudio(MultipartFile file, Integer keyFamilyId, String folderName) {
        KeyFamily storedKeyFamily = keyFamilyRepository.findByKeyFamilyId(keyFamilyId).orElseThrow(() -> new RuntimeException("KeyFamily not found with keyFamilyId: " + keyFamilyId));
        String keyFamilyFileName = file.getOriginalFilename();
        KeyFamilyAudio storedKeyFamilyAudio = keyFamilyAudioRepository.findByKeyFamilyIdAndKeyFamilyAudioFileName(keyFamilyId, keyFamilyFileName);
        if(storedKeyFamilyAudio != null){
            throw new RuntimeException("KeyFamily Audio already exists with name: " + keyFamilyFileName);
        }
//        KeyFamilyResponse keyFamilyResponse = null;
//        KeyFamilyAudio defaultKeyFamilyAudio = storedKeyFamily.getDefaultKeyFamilyAudio();
                KeyFamilyAudio defaultKeyFamilyAudio = null;
        Optional<KeyFamilyAudio> keyFamilyAudioOptional = storedKeyFamily.getKeyFamilyAudioSet().stream().filter(au -> au.isDefault()).findFirst();
        if(keyFamilyAudioOptional.isPresent()){
            defaultKeyFamilyAudio = keyFamilyAudioOptional.get();
        }
        KeyFamilyAudio keyFamilyAudio = KeyFamilyAudio.builder().build();
        try{
            String awsS3FileURl = saveFileToAWSS3Bucket(file, "video/mp4", folderName);

            if(defaultKeyFamilyAudio == null){
                keyFamilyAudio.setKeyFamilyId(keyFamilyId);
                keyFamilyAudio.setDefault(true);
                keyFamilyAudio.setKeyFamilyAudioUrl(awsS3FileURl);
                keyFamilyAudio.setKeyFamilyAudioFileName(keyFamilyFileName);
                storedKeyFamily.addKeyFamilyAudio(keyFamilyAudio);
//                storedKeyFamily.setDefaultKeyFamilyAudio(keyFamilyAudio);
                storedKeyFamily = keyFamilyRepository.save(storedKeyFamily);
//                keyFamilyResponse = appObjectMapper.mapKeyFamilyToKeyFamilyResponse(storedKeyFamily);
                return keyFamilyDefaultMapper.toKeyFamilyDefaultDTO(storedKeyFamily);
            }else{
                keyFamilyAudio.setKeyFamilyId(keyFamilyId);
                keyFamilyAudio.setKeyFamilyAudioUrl(awsS3FileURl);
                keyFamilyAudio.setKeyFamilyAudioFileName(keyFamilyFileName);
                storedKeyFamily.addKeyFamilyAudio(keyFamilyAudio);
                storedKeyFamily = keyFamilyRepository.save(storedKeyFamily);
//                keyFamilyResponse = appObjectMapper.mapKeyFamilyToKeyFamilyResponse(storedKeyFamily);
                return keyFamilyDefaultMapper.toKeyFamilyDefaultDTO(storedKeyFamily);
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
//        return keyFamilyResponse;
    }
}
