package client;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import commons.Event;

public class Websocket {
    private final String eventID;
    private StompSession stompSession;

    /**
     * Websocket client constructor
     *
     * @param eventID event invite code to connect to
     */
    public Websocket(String eventID) {
        this.eventID = eventID;
    }
    public static void main(String[] args) {
        new Websocket("ABCDE").connect();
    }
    public void connect() {
        CountDownLatch latch = new CountDownLatch(1);

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

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
        Scanner scanner = new Scanner(System.in);
        while(true) {
            String msg = scanner.nextLine();
            if(msg.isEmpty()) break;
            Event event = new Event(msg);
            System.out.println("Sending event\n" + event);
            send(event);
        }
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
    public void send(Event msg) {
        stompSession.send("/app/" + eventID, msg);
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {

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
            return Event.class;
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
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
        }
    }
}


