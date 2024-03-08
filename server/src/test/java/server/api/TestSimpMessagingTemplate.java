package server.api;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

public class TestSimpMessagingTemplate extends SimpMessagingTemplate {
    private String destination;
    private Object payload;
    private Map<String, Object> headers;

    /**
     * Create a new {@link SimpMessagingTemplate} instance.
     *
     * @param messageChannel the message channel (never {@code null})
     */
    public TestSimpMessagingTemplate(MessageChannel messageChannel) {
        super(messageChannel);
    }

    /**
     * @param destination the target destination
     * @param payload     the Object to use as payload
     * @param headers     the headers for the message to send
     * @throws MessagingException
     */
    @Override
    public void convertAndSend(String destination, Object payload,
                               Map<String, Object> headers) throws MessagingException {
        this.destination = destination;
        this.payload = payload;
        this.headers = headers;
    }

    /**
     * @return destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @return payload
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * @return headers
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * @return string representation of current property values of this
     */
    @Override
    public String toString() {
        return "TestSimpMessagingTemplate{" +
                "destination='" + destination + '\'' +
                ", payload=" + payload +
                ", headers=" + headers +
                '}';
    }
}
