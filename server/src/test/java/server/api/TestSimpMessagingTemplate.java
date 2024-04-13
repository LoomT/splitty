package server.api;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestSimpMessagingTemplate extends SimpMessagingTemplate {
    private final List<String> destination;
    private final List<Object> payload;
    private final List<Map<String, Object>> headers;

    /**
     * Create a new {@link SimpMessagingTemplate} instance.
     *
     * @param messageChannel the message channel (never {@code null})
     */
    public TestSimpMessagingTemplate(MessageChannel messageChannel) {
        super(messageChannel);
        destination = new ArrayList<>();
        payload = new ArrayList<>();
        headers = new ArrayList<>();
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
        this.destination.add(destination);
        this.payload.add(payload);
        this.headers.add(headers);
    }

    /**
     * @return last destination
     */
    public String getDestination() {
        return destination.getLast();
    }

    /**
     * @return last payload
     */
    public Object getPayload() {
        return payload.getLast();
    }

    /**
     * @return last headers
     */
    public Map<String, Object> getHeaders() {
        return headers.getLast();
    }

    /**
     * @return all destinations
     */
    public List<String> getAllDestinations() {
        return destination;
    }

    /**
     * @return all payloads
     */
    public List<Object> getAllPayloads() {
        return payload;
    }

    /**
     * @return all headers
     */
    public List<Map<String, Object>> getAllHeaders() {
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
