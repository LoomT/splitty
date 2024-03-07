package client;

import client.scenes.EventPageCtrl;
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

public class Websocket {
    private final EventPageCtrl ctrl;

    /**
     * Websocket client constructor
     *
     * @param ctrl event page controller
     */
    public Websocket(EventPageCtrl ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * @param eventID event id
     */
    public void connect(String eventID) {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        List<MessageConverter> converterList = List.of(new MappingJackson2MessageConverter(),
                new StringMessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converterList));

        String url = "ws://localhost:8080/ws"; //TODO inject this
        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        StompSession stompSession;
        try {
            stompSession = stompClient.connectAsync(url, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not connect to server", e);
        }
        // Subscribe to specific event channel
        stompSession.subscribe("/event/" + eventID, sessionHandler);

    }
    private class MyStompSessionHandler extends StompSessionHandlerAdapter {

        private static final Map<String, Type> typeMap = new HashMap<>(Map.of(
                "commons.Event", Event.class,
                "commons.Participant", Participant.class,
                "commons.Expense", Expense.class,
                "java.lang.String", String.class));

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
            System.out.println("Received\n" + payload);
            String action = headers.get("action").getFirst();
            switch(action) {
                case "titleChange" -> ctrl.changeTitle((String)payload);
//                case "deleteEvent" -> ctrl.deleteEvent();
//                case "addParticipant" -> ctrl.addParticipant((Participant)payload);
//                case "updateParticipant" -> ctrl.updateParticipant((Participant)payload);
//                case "removeParticipant" -> ctrl.removeParticipant((Long)payload);
//                case "addExpense" -> ctrl.addExpense((Expense)payload);
//                case "updateExpense" -> ctrl.updateExpense((Expense)payload);
//                case "removeExpense" -> ctrl.removeExpense((Long)payload);
            }
        }

        @Override
        public void handleException(@NonNull StompSession session, StompCommand command,
                                    @NonNull StompHeaders headers, @NonNull byte[] payload,
                                    @NonNull Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
        }
    }
}


