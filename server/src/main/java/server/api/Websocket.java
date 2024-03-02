package server.api;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class Websocket {
    @MessageMapping("/news")
    @SendTo("/topic/news")
    public String broadcast(@Payload String message) {
        System.out.println("messaged");
        return message;
    }
}
