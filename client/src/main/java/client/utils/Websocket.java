package client.utils;

import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.WebsocketActions;
import javafx.application.Platform;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class Websocket {
    private StompSession stompSession;
    private final StompSessionHandler sessionHandler;
    private final WebSocketStompClient stompClient;
    private final String url;
    private final EnumMap<WebsocketActions, Set<Consumer<Object>>> functions;

    /**
     * Websocket client constructor
     *
     * @param config config for url of the websocket address
     */
    @Inject
    public Websocket(UserConfig config) {
        // Initialize the enum map with all enum values
        functions = new EnumMap<>(WebsocketActions.class);
        resetAllActions();

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        List<MessageConverter> converterList = List.of(new MappingJackson2MessageConverter(),
                new StringMessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converterList));

        this.url = "ws:" + config.getUrl() + "ws";
        sessionHandler = new MyStompSessionHandler();
    }

    /**
     * Subscribe to updates of a particular event
     *
     * @param eventID event id
     */
    public void connect(String eventID) {
        if(stompSession != null && stompSession.isConnected()) return;
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
        if (stompSession == null || !stompSession.isConnected()) return;
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
     * @param action enum name of the function
     * @param consumer function that consumes type of payload and payload in that order
     */
    public void on(WebsocketActions action, Consumer<Object> consumer) {
        functions.get(action).add(consumer);
    }


    /**
     * Registers all the change listeners on WS if they're not registered already
     * @param event the event in which we listen on the participant changes
     * @param updatePartCallback this is called when a participant in the event is updated
     * @param addPartCallback this is called when a participant in the event is created
     * @param deletePartCallback this is called when a participant in the event is deleted
     */
    public void registerParticipantChangeListener(
            Event event,
            Consumer<Event> updatePartCallback,
            Consumer<Event> addPartCallback,
            Consumer<Event> deletePartCallback
    ) {
        this.resetAction(WebsocketActions.UPDATE_PARTICIPANT);
        this.resetAction(WebsocketActions.ADD_PARTICIPANT);
        this.resetAction(WebsocketActions.REMOVE_PARTICIPANT);

        this.on(WebsocketActions.UPDATE_PARTICIPANT, (Object part)->{
            Participant p = (Participant) part;
            int index = -1;
            for (int i = 0; i < event.getParticipants().size(); i++) {
                Participant curr = event.getParticipants().get(i);
                if (curr.getId() == p.getId()) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The updated participant's ID ("
                        + p.getId()+
                        ") does not match with any ID's of the already existing participants");
            }
            event.getParticipants().remove(index);
            event.getParticipants().add(index, p);
            updatePartCallback.accept(event);
        });
        this.on(WebsocketActions.ADD_PARTICIPANT, (Object part) -> {
            Participant p = (Participant) part;
            event.getParticipants().add(p);
            addPartCallback.accept(event);
        });
        this.on(WebsocketActions.REMOVE_PARTICIPANT, (Object part) -> {
            long partId = (long) part;
            int index = -1;
            for (int i = 0; i < event.getParticipants().size(); i++) {
                Participant curr = event.getParticipants().get(i);
                if (curr.getId() == partId) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The deleted participant's ID ("
                        + partId+
                        ") does not match with any ID's of the already existing participants");
            }
            event.getParticipants().remove(index);
            deletePartCallback.accept(event);
        });
    }

    /**
     * Makes the participants of an expense share the same instances as the participants of an event
     *
     * @param expense expense for which participants to link
     * @param participants participant list from event
     */
    public void linkExpenseParticipants(Expense expense, List<Participant> participants) {
        expense.setExpenseAuthor(participants.stream()
                .filter(p -> p.equals(expense.getExpenseAuthor())).findFirst().orElseThrow());
        expense.setExpenseParticipants(participants.stream()
                .filter(p -> expense.getExpenseParticipants().contains(p)).toList());
    }

    /**
     * Registers all the change listeners on WS if they're not registered already
     * @param event the event in which we listen on the participant changes
     * @param updateExpCallback this is called when a participant in the event is updated
     * @param addExpCallback this is called when a participant in the event is created
     * @param deleteExpCallback this is called when a participant in the event is deleted
     *
     */

    public void registerExpenseChangeListener(
            Event event,
            Consumer<Event> updateExpCallback,
            Consumer<Event> addExpCallback,
            Consumer<Event> deleteExpCallback
    ) {
        this.resetAction(WebsocketActions.UPDATE_EXPENSE);
        this.resetAction(WebsocketActions.ADD_EXPENSE);
        this.resetAction(WebsocketActions.REMOVE_EXPENSE);

        this.on(WebsocketActions.ADD_EXPENSE, (Object exp) -> {
            Expense expense = (Expense) exp;
            linkExpenseParticipants(expense, event.getParticipants());
            event.getExpenses().add(expense);
            addExpCallback.accept(event);
        });
        this.on(WebsocketActions.UPDATE_EXPENSE, (Object exp) -> {
            updateExpense(event, (Expense)exp);
            updateExpCallback.accept(event);
        });
        this.on(WebsocketActions.REMOVE_EXPENSE, (Object exp) -> {
            long expId = (long) exp;
            if(!event.getExpenses().removeIf(expense -> expense.getId() == expId))
                throw new RuntimeException("The deleted expense's ID ("
                    + expId +
                    ") does not match with any ID's of the already existing expenses");
            deleteExpCallback.accept(event);
        });

    }

    /**
     * @param event event of which expense to update
     * @param expense new expense
     */
    private void updateExpense(Event event, Expense expense) {
        int index = -1;
        for (int i = 0; i < event.getExpenses().size(); i++) {
            Expense curr = event.getExpenses().get(i);
            if (curr.getId() == expense.getId()) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new RuntimeException("The updated expense's ID ("
                    + expense.getId()+
                    ") does not match with any ID's of the already existing expenses");
        }
        event.getExpenses().remove(index);
        linkExpenseParticipants(expense, event.getParticipants());
        event.getExpenses().add(index, expense);
    }

    /**
     * Removes all listeners set for a particular action
     *
     * @param action websocket action to reset all listeners for
     */
    public void resetAction(WebsocketActions action) {
        functions.put(action, new HashSet<>());
    }

    /**
     * Resets all action listeners
     */
    public void resetAllActions() {
        EnumSet.allOf(WebsocketActions.class).forEach(this::resetAction);
    }

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
            System.out.println("WS connected");
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
            try {
                WebsocketActions action = WebsocketActions
                        .valueOf(headers.get("action").getFirst());
                functions.get(action).forEach(consumer -> {
                    // This is necessary to run the Javafx updates on the same
                    // thread as the app is run on, and not the WS thread
                    Platform.runLater(() -> consumer.accept(payload));
                });

            } catch (IllegalArgumentException e) {
                System.out.println("Server sent an unknown action");
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
