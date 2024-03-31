package utils;

import commons.Event;
import client.utils.Websocket;
import commons.Expense;
import commons.Participant;
import commons.WebsocketActions;
import javafx.application.Platform;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TestWebsocket implements Websocket {

    private String eventID;
    private boolean connected = false;
    private final EnumMap<WebsocketActions, Consumer<Object>> functions;


    /**
     * Constructor for the TestWebsocket
     *
     */
    public TestWebsocket() {
        functions = new EnumMap<>(WebsocketActions.class);
    }

    /**
     *
     * @param eventID the event id to connect to
     */
    @Override
    public void connect(String eventID) {
        this.eventID = eventID;
        this.connected = true;
        System.out.println("Connected to event with ID: " + eventID);
    }

    /**
     * Disconnect from the event
     *
     */
    @Override
    public void disconnect() {
        this.connected = false;
        this.eventID = null;
        functions.clear();
        System.out.println("Disconnected from event");
    }

    /**
     *
     *
     * @param action enum name of the function
     * @param consumer function that consumes type of payload and payload in that order
     */
    @Override
    public void on(WebsocketActions action, Consumer<Object> consumer) {
        functions.put(action, consumer);
    }

    /**
     *
     *
     * @param event the event in which we listen on the participant changes
     * @param updatePartCallback this is called when a participant in the event is updated
     * @param addPartCallback this is called when a participant in the event is created
     * @param deletePartCallback this is called when a participant in the event is deleted
     */

    public void registerParticipantChangeListener(
            Event event,
            Consumer<Event> updatePartCallback,
            Consumer<Event> addPartCallback,
            Consumer<Event> deletePartCallback) {

        this.resetAction(WebsocketActions.UPDATE_PARTICIPANT);
        this.resetAction(WebsocketActions.ADD_PARTICIPANT);
        this.resetAction(WebsocketActions.REMOVE_PARTICIPANT);

        this.on(WebsocketActions.UPDATE_PARTICIPANT, (Object part) -> {
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
                        + p.getId() +
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
                        + partId +
                        ") does not match with any ID's of the already existing participants");
            }
            event.getParticipants().remove(index);
            deletePartCallback.accept(event);
        });
    }

    /**
     *
     *
     * @param event the event in which we listen on the participant changes
     * @param updateExpCallback this is called when a participant in the event is updated
     * @param addExpCallback this is called when a participant in the event is created
     * @param deleteExpCallback this is called when a participant in the event is deleted
     */

    @Override
    public void registerExpenseChangeListener(
            Event event,
            Consumer<Event> updateExpCallback,
            Consumer<Event> addExpCallback,
            Consumer<Event> deleteExpCallback)
    {

        this.resetAction(WebsocketActions.UPDATE_EXPENSE);
        this.resetAction(WebsocketActions.ADD_EXPENSE);
        this.resetAction(WebsocketActions.REMOVE_PARTICIPANT);

        this.on(WebsocketActions.ADD_EXPENSE, (Object exp) -> {
            Expense expense = (Expense) exp;
            event.getExpenses().add(expense);
            addExpCallback.accept(event);
        });
        this.on(WebsocketActions.UPDATE_EXPENSE, (Object exp) -> {
            Expense expense = (Expense) exp;
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
                        + expense.getId() +
                        ") does not match with any ID's of the already existing expenses");
            }
            event.getExpenses().remove(index);
            event.getExpenses().add(index, expense);
            updateExpCallback.accept(event);
        });
        this.on(WebsocketActions.REMOVE_EXPENSE, (Object exp) -> {
            long expId = (long) exp;
            int index = -1;
            for (int i = 0; i < event.getExpenses().size(); i++) {
                Expense curr = event.getExpenses().get(i);
                if (curr.getId() == expId) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The deleted expense's ID ("
                        + expId +
                        ") does not match with any ID's of the already existing expenses");
            }
            event.getExpenses().remove(index);
            deleteExpCallback.accept(event);
        });
    }

    /**
     *
     * @param action websocket action to reset all listeners for
     */
    @Override
    public void resetAction(WebsocketActions action) {
        functions.remove(action);
    }

    /**
     * Resets all listeners
     *
     */

    @Override
    public void resetAllActions() {
        functions.clear();
    }

    /**
     * simulateAction is used to simulate an action from the server
     *
     * @param action
     * @param payload
     */

    public void simulateAction(WebsocketActions action, Object payload) {
        if (functions.containsKey(action)) {
            functions.get(action).accept(payload);
        } else {
            System.out.println("No listener for action: " + action);
        }
    }

    /**
     * isConnected is used to check if the websocket is connected
     * @return true if connected, false otherwise (boolean)
     */

    public boolean isConnected() {
        return connected;
    }


    /**
     * String representation of the event ID
      * @return eventID
     */
    public String getEventID() {
        return eventID;
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
         * @param session          stomp session
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
                WebsocketActions action = WebsocketActions.valueOf(
                        headers.get("action").getFirst());
                Consumer<Object> consumer = functions.get(action);
                if (consumer != null) {
                    // This is necessary to run the Javafx updates on the same
                    // thread as the app is run on, and not the WS thread
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            consumer.accept(payload);
                            // This keeps the accept method but used directly
                        }
                    });
                }
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
