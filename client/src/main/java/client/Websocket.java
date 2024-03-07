package client;

import client.scenes.EventPageCtrl;
import commons.Event;
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
    private String eventID;
    private StompSession stompSession;
    private EventPageCtrl ctrl;

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
        this.eventID = eventID;
//        CountDownLatch latch = new CountDownLatch(1);

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        List<MessageConverter> converterList = List.of(new MappingJackson2MessageConverter(),
                new StringMessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converterList));

        String url = "ws://localhost:8080/ws"; //TODO inject this
        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        try {
            stompSession = stompClient.connectAsync(url, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not connect to server", e);
        }
        // Subscribe to specific event channel
        stompSession.subscribe("/event/" + eventID, sessionHandler);

        // Testing
//        Scanner scanner = new Scanner(System.in);
//        while(true) {
//            String msg = scanner.nextLine();
//            if(msg.isEmpty()) break;
//            Event event = new Event(msg);
//            System.out.println("Sending event\n" + event);
//            send(event);
//        }
////        try {
////            latch.await();
////        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
////        }
    }

    /**
     * @param msg message to send
     */
    public void send(Event msg) {
        stompSession.send("/app/" + eventID, msg);
    }

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {

        private final Map typeMap;

        public MyStompSessionHandler() {
            typeMap = new HashMap<>(Map.of("commons.Event", Event.class,
                    "java.lang.String", String.class));
        }

        /**
         * Executes after successfully connecting to the server
         *
         * @param session
         * @param connectedHeaders
         */
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("connected");
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return (Type) typeMap.get(headers.get("type").getFirst());
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
            if(headers.get("action").getFirst().equals("titleChange")) {
                String title = (String)payload;
                ctrl.changeTitle(title);
            }
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
        }
    }
}


