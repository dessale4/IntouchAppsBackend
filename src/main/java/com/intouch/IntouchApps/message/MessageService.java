package com.intouch.IntouchApps.message;

import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import com.intouch.IntouchApps.utils.AppDateUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@RefreshScope
@Slf4j
@Transactional
public class MessageService {
    @Value("${application.messages.contactus.email}")
    private String supportEmail;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageReplyRepository messageReplyRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
    public MessageResponse createContactUsMessage(MessageRequest messageRequest){
        String receivingEmail = messageRequest.getReceivingEmail() != null ? messageRequest.getReceivingEmail() :supportEmail;
        String encryptedReceivingEmail = standardPBEStringEncryptor.encrypt(receivingEmail.toLowerCase());
        String encryptedSendingEmail = standardPBEStringEncryptor.encrypt(messageRequest.getSendingEmail().toLowerCase());
        if(messageRequest.getSendingEmail().equals(receivingEmail)){
            throw new RuntimeException("Self messaging is not allowed");
        }
        User sendingUser  = userRepository.findByEmail(encryptedSendingEmail).orElseThrow(()->new UsernameNotFoundException("No account with email " + messageRequest.getSendingEmail()));
        User recievingUser  = userRepository.findByEmail(encryptedReceivingEmail).orElseThrow(()->new RuntimeException("Something went wrong"));
//        Random random = new Random();
        long randomNumber = getRandomNumber(1,3);
        AppMessage appMessage = AppMessage.builder()
                .message(messageRequest.getMessage())
//                !!!!recheck .plusDays(randomNumber) should be removed
//                .createdDate(AppDateUtil.getCurrentUTCDateTime().plusDays(randomNumber))//for test only
//                .createdDate(AppDateUtil.getCurrentUTCLocalDateTime()) //for real use
                .receivingEmail(recievingUser.getEmail())
                .sendingEmail(sendingUser.getEmail())
                .sendingUsername(sendingUser.getPublicUserName())
                .receivingUsername(recievingUser.getPublicUserName())
                .messageReplies(List.of())//0 replies at first
//                .lastModifiedDate(AppDateUtil.getCurrentUTCLocalDateTime())
                .build();
        AppMessage savedMessage = messageRepository.save(appMessage);
        MessageResponse messageResponse = mapToMessageResponse(savedMessage);
        return messageResponse;
    }

    private MessageResponse mapToMessageResponse(AppMessage savedMessage) {
       return MessageResponse.builder()
//                .lastModifiedDate(savedMessage.getLastModifiedDate().toLocalDate())
               .lastModifiedDate(savedMessage.getUpdatedAt().toLocalDate())
                .lastModifiedTime(savedMessage.getUpdatedAt().toLocalTime())
               .createdDateTime(savedMessage.getCreatedAt())
                .createdDate(savedMessage.getCreatedAt().toLocalDate())
                .createdTime(savedMessage.getCreatedAt().toLocalTime())
                .messageId(savedMessage.getId())
                .message(savedMessage.getMessage())
                .messageReplies(mapMessageRepliesToMessageReplyDTOs(savedMessage.getMessageReplies()))
                .sendingUsername(savedMessage.getSendingUsername())
                .sendingEmail(standardPBEStringEncryptor.decrypt(savedMessage.getSendingEmail()) )
                .receivingEmail(standardPBEStringEncryptor.decrypt(savedMessage.getReceivingEmail()))
                .receivingUsername(savedMessage.getReceivingUsername())
                .build();
    }

    private List<MessageReplyDTO> mapMessageRepliesToMessageReplyDTOs(List<MessageReply> messageReplies){
        return messageReplies.stream()
                .map(m->mapMessageReplyToMessageReplyDTO(m))
                .collect(Collectors.toList());
    }
    private MessageReplyDTO mapMessageReplyToMessageReplyDTO (MessageReply messageReply){
        return MessageReplyDTO.builder()
                .id(messageReply.getId())
                .sendingUsername(messageReply.getSendingUsername())
                .message(messageReply.getMessage())
                .sendingEmail(standardPBEStringEncryptor.decrypt(messageReply.getSendingEmail()))
                .createdDate(messageReply.getCreatedAt())
                .build();
    }

    @Transactional
    public List<MessageResponse> getUserContactUsMessages(String sendingEmail) {
      List<AppMessage> userContactUsMessages = messageRepository.findUserContactUsMessages(standardPBEStringEncryptor.encrypt(sendingEmail.toLowerCase()));
        List<MessageResponse> messageResponses = userContactUsMessages.stream()
                .sorted(Comparator.comparing(AppMessage::getCreatedAt))
                .map(m -> mapToMessageResponse(m))
                .collect(Collectors.toList());
        return messageResponses;
    }
    public void deleteContactUsMessage(Integer messageId, Principal auth) {
        AppMessage savedMessage = messageRepository.findById(messageId).orElseThrow(()->new RuntimeException("Something went wrong"));
        String loggedEmail = auth.getName();

        boolean isAuthenticatedToDelete = savedMessage.getSendingEmail().equals(loggedEmail) || loggedEmail.equals(standardPBEStringEncryptor.encrypt(supportEmail.toLowerCase()));
        if(isAuthenticatedToDelete){
            messageRepository.delete(savedMessage);
        }else{
            throw new RuntimeException("You are not allowed to do so");
        }
    }
    public void rePlyToMessage(String replyingEmail, Integer messageId, String messageReply) {
        String encryptedReplyingEmail = standardPBEStringEncryptor.encrypt(replyingEmail.toLowerCase());
        User replyingUser  = userRepository.findByEmail(encryptedReplyingEmail).orElseThrow(()->new UsernameNotFoundException("Not allowed to do so"));
        AppMessage storedMessage = messageRepository.findById(messageId).orElseThrow(()-> new RuntimeException("Something went wrong"));
        if(replyingEmail.equals(standardPBEStringEncryptor.decrypt(storedMessage.getReceivingEmail())) || replyingEmail.equals(standardPBEStringEncryptor.decrypt(storedMessage.getSendingEmail()))){
            MessageReply messageToReply = MessageReply.builder()
                    .message(messageReply)
                    .sendingEmail(encryptedReplyingEmail)
                    .sendingUsername(replyingUser.getPublicUserName())
//                    .createdDate(AppDateUtil.getCurrentUTCLocalDateTime())
                    .build();
            messageToReply = messageReplyRepository.save(messageToReply);
            storedMessage.addMessageReply(messageToReply);
            messageRepository.save(storedMessage);
        }else{
            throw new RuntimeException("Not Allowed to do so.");
        }
    }
}
