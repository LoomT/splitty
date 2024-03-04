package server.api;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class Websocket {
    /**
     * @param message message received from client
     * @param id event id
     * @return the same message
     */
    @MessageMapping("/event/{id}")
    @SendTo("/event/{id}")
    public String broadcast(@Payload String message, @DestinationVariable String id) {
        System.out.println("Server received message " + message + " on channel " + id);
        return message;
    }
}
