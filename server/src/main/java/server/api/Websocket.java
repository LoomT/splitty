package server.api;

import commons.Event;
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
    @MessageMapping("/{id}")
    @SendTo("/event/{id}")
    public Event broadcast(@Payload Event message, @DestinationVariable String id) {
        System.out.println("Server received message " + message + " on channel " + id);
        message.setId("id");
        return message;
    }
}
