package utils;

import client.utils.Websocket;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.WebsocketActions;

import java.util.*;
import java.util.function.Consumer;

public class TestWebsocket implements Websocket {

    private String eventID;
    private boolean connected = false;
    private final EnumMap<WebsocketActions, Set<Consumer<Object>>> functions;

    private final List<WebsocketActions> triggeredActions = new ArrayList<>();
    private final List<Object> payloads = new ArrayList<>();


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
        functions.computeIfAbsent(action, k -> new HashSet<>()).add(consumer);
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
     * simulateAction is used to simulate an action from the server.
     * It triggers all registered consumers for the specified action.
     *
     * @param action The WebSocket action to simulate.
     * @param payload The payload to pass to the consumers for this action.
     */
    public void simulateAction(WebsocketActions action, Object payload) {
        Set<Consumer<Object>> consumers = functions.get(action);
        if (consumers != null && !consumers.isEmpty()) {
            for (Consumer<Object> consumer : consumers) {
                consumer.accept(payload);
            }
            triggeredActions.add(action);
            payloads.add(payload);
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


    /**
     * Checks if a specific action has been triggered
     *
     * @param action The action to check for
     * @return true if the action has been triggered, false otherwise
     */
    public boolean hasActionBeenTriggered(WebsocketActions action) {
        return triggeredActions.contains(action);
    }

    /**
     * Checks if a specific payload has been sent
     *
     * @param expectedPayload The payload to check for
     * @return true if the payload has been sent, false otherwise
     */
    public boolean hasPayloadBeenSent(Object expectedPayload) {
        return payloads.stream().anyMatch(payload -> payload.equals(expectedPayload));
    }

    /**
     * Resets the list of triggered actions and payloads
     */

    public void resetTriggers() {
        triggeredActions.clear();
        payloads.clear();
    }
}

