package server.api;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

public class SimpMessagingTemplateTest extends SimpMessagingTemplate {
    public String destination;
    public Object payload;
    public Map<String, Object> headers;

    /**
     * Create a new {@link SimpMessagingTemplate} instance.
     *
     * @param messageChannel the message channel (never {@code null})
     */
    public SimpMessagingTemplateTest(MessageChannel messageChannel) {
        super(messageChannel);
    }

    @Override
    public void convertAndSend(String destination, Object payload, Map<String, Object> headers) throws MessagingException {
        this.destination = destination;
        this.payload = payload;
        this.headers = headers;
    }
    @Override
    public String toString() {
        return "SimpMessagingTemplateTest{" +
                "destination='" + destination + '\'' +
                ", payload=" + payload +
                ", headers=" + headers +
                '}';
    }
}
