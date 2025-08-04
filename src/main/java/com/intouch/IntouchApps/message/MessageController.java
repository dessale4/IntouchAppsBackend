package com.intouch.IntouchApps.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("message")
public class MessageController {
    private final MessageService messageService;
    @PostMapping(value = "/contactus")
    public MessageResponse contactUs(@RequestBody MessageRequest messageRequest){
        return messageService.createContactUsMessage(messageRequest);
    }
    @GetMapping(value = "/contactus")
    public List<MessageResponse> userContactUsMessages(@RequestParam("sendingEmail") String sendingEmail){
       return messageService.getUserContactUsMessages(sendingEmail);
    }
    @PostMapping(value = "/replyToMessage")
    public void rePlyToMessage(
            @RequestParam("replyingEmail") String replyingEmail,
            @RequestParam("messageId") Integer messageId,
            @RequestParam("messageReply") String messageReply
    ){
    messageService.rePlyToMessage(replyingEmail, messageId, messageReply);
    }
    @DeleteMapping(value = "/contactus")
    public void deleteContactUsMessage(@RequestParam("messageId") Integer messageId, Principal auth){
        messageService.deleteContactUsMessage(messageId, auth);
    }

}
