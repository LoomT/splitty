package client;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        //stompClient.setTaskScheduler(taskScheduler); // for heartbeats

        String url = "ws://localhost:8080/test";
        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        StompSession stompSession = stompClient.connectAsync(url, sessionHandler).get();
        stompSession.subscribe("/topic/news", sessionHandler);
        stompSession.send("/topic/news", "hello");
        latch.await();
    }
    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("connected");
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println(payload);
        }
    }
}


