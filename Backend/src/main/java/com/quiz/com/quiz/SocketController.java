
package com.quiz.com.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SocketController {

    @MessageMapping("/sendMessage") // Handle messages sent to /app/sendMessage
    @SendTo("/topic/messages") // Broadcast to all clients subscribed to /topic/messages
    public String handleMessage(String message) {
        return "Server received: " + message;
    }
}