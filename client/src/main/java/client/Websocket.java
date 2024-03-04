package client;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

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
        stompClient.setMessageConverter(new StringMessageConverter());

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
            System.out.println("Sending " + msg);
            send(msg);
        }
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
    public void send(String msg) {
        stompSession.send("/app/event/" + eventID, msg);
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
            return super.getPayloadType(headers);
        }

        /**
         * Executes when client receives a message from the server
         *
         * @param headers headers
         * @param payload message body
         */
        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Received " + payload);
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
        }
    }
}


