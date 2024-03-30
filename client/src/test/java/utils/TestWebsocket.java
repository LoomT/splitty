package utils;

import commons.Event;
import client.utils.Websocket;
import commons.WebsocketActions;

import java.util.EnumMap;
import java.util.function.Consumer;

public class TestWebsocket implements Websocket {

    private String eventID;
    private boolean connected = false;
    private final EnumMap<WebsocketActions,
            Consumer<Object>> actionListeners = new EnumMap<>(WebsocketActions.class);

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
        actionListeners.clear();
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
        actionListeners.put(action, consumer);
    }

    /**
     *
     *
     * @param event the event in which we listen on the participant changes
     * @param updatePartCallback this is called when a participant in the event is updated
     * @param addPartCallback this is called when a participant in the event is created
     * @param deletePartCallback this is called when a participant in the event is deleted
     */

    @Override
    public void registerParticipantChangeListener(
            Event event,
            Consumer<Event> updatePartCallback,
            Consumer<Event> addPartCallback,
            Consumer<Event> deletePartCallback)
    {
        this.on(WebsocketActions.UPDATE_PARTICIPANT, (obj) -> updatePartCallback.accept(event));
        this.on(WebsocketActions.ADD_PARTICIPANT, (obj) -> addPartCallback.accept(event));
        this.on(WebsocketActions.REMOVE_PARTICIPANT, (obj) -> deletePartCallback.accept(event));
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
        this.on(WebsocketActions.UPDATE_EXPENSE, (obj) -> updateExpCallback.accept(event));
        this.on(WebsocketActions.ADD_EXPENSE, (obj) -> addExpCallback.accept(event));
        this.on(WebsocketActions.REMOVE_EXPENSE, (obj) -> deleteExpCallback.accept(event));
    }

    /**
     *
     * @param action websocket action to reset all listeners for
     */
    @Override
    public void resetAction(WebsocketActions action) {
        actionListeners.remove(action);
    }

    /**
     * Resets all listeners
     *
     */

    @Override
    public void resetAllActions() {
        actionListeners.clear();
    }

    /**
     * simulateAction is used to simulate an action from the server
     *
     * @param action
     * @param payload
     */

    public void simulateAction(WebsocketActions action, Object payload) {
        if (actionListeners.containsKey(action)) {
            actionListeners.get(action).accept(payload);
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
}
