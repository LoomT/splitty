package client.utils;

import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class Websocket {
    private StompSession stompSession;
    private final StompSessionHandler sessionHandler;
    private final WebSocketStompClient stompClient;
    private final String url;
    private final Map<String, Consumer<Object>> functions;

    /**
     * Websocket client constructor
     *
     * @param config config for url of the websocket address
     */
    @Inject
    public Websocket(UserConfig config) {
        functions = new HashMap<>();
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        List<MessageConverter> converterList = List.of(new MappingJackson2MessageConverter(),
                new StringMessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converterList));

        this.url = "ws:" + config.getUrl() + "ws";
        sessionHandler = new MyStompSessionHandler();
    }

    /**
     * @param eventID event id
     */
    public void connect(String eventID) {
        try {
            stompSession = stompClient.connectAsync(url, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not connect to server", e);
        }
        // Subscribe to specific event channel
        stompSession.subscribe("/event/" + eventID, sessionHandler);
    }

    /**
     * Disconnect the websocket from the server
     */
    public void disconnect() {
        stompSession.disconnect();
    }

    /**
     * Sets the function for provided name
     * <pre>
     * available functions:
     * titleChange(String)
     * deleteEvent()
     * addParticipant(Participant)
     * updateParticipant(Participant)
     * removeParticipant(id)
     * addExpense(Expense)
     * updateExpense(Expense)
     * removeExpense(id)
     * </pre>
     * @param name name of the function
     * @param consumer function that consumes type of payload and payload in that order
     */
    public void on(String name, Consumer<Object> consumer) {functions.put(name, consumer);}

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {

        private static final Map<String, Type> typeMap = new HashMap<>(Map.of(
                "commons.Event", Event.class,
                "commons.Participant", Participant.class,
                "commons.Expense", Expense.class,
                "java.lang.String", String.class,
                "java.lang.Long", Long.class));

        /**
         * Executes after successfully connecting to the server
         *
         * @param session stomp session
         * @param connectedHeaders headers of the message
         */
        @Override
        public void afterConnected(@NonNull StompSession session,
                                   @NonNull StompHeaders connectedHeaders) {
            System.out.println("connected");
        }

        @Override
        @NonNull
        public Type getPayloadType(StompHeaders headers) {
            return typeMap.get(headers.get("type").getFirst());
        }

        /**
         * Executes when client receives a message from the server
         *
         * @param headers headers
         * @param payload message body
         */
        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            String action = headers.get("action").getFirst();
            functions.get(action).accept(payload);
        }

        @Override
        public void handleException(@NonNull StompSession session, StompCommand command,
                                    @NonNull StompHeaders headers, @NonNull byte[] payload,
                                    @NonNull Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
        }
    }
}


