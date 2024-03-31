package client.utils;

import commons.Event;
import commons.WebsocketActions;

import java.util.function.Consumer;

public interface Websocket {

    /**
     * Connect the websocket to the server
     * @param eventID the event id to connect to
     */
    void connect(String eventID);

    /**
     * Disconnect the websocket from the server
     */
    void disconnect();

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
    void on(WebsocketActions action, Consumer<Object> consumer);


    /**
     * Registers all the change listeners on WS if they're not registered already
     * @param event the event in which we listen on the participant changes
     * @param updatePartCallback this is called when a participant in the event is updated
     * @param addPartCallback this is called when a participant in the event is created
     * @param deletePartCallback this is called when a participant in the event is deleted
     */
    void registerParticipantChangeListener(
            Event event,
            Consumer<Event> updatePartCallback,
            Consumer<Event> addPartCallback,
            Consumer<Event> deletePartCallback
    );


    /**
     * Registers all the change listeners on WS if they're not registered already
     * @param event the event in which we listen on the participant changes
     * @param updateExpCallback this is called when a participant in the event is updated
     * @param addExpCallback this is called when a participant in the event is created
     * @param deleteExpCallback this is called when a participant in the event is deleted
     *
     */

    void registerExpenseChangeListener(
            Event event,
            Consumer<Event> updateExpCallback,
            Consumer<Event> addExpCallback,
            Consumer<Event> deleteExpCallback

    );

    /**
     * Removes all listeners set for a particular action
     *
     * @param action websocket action to reset all listeners for
     */
    void resetAction(WebsocketActions action);

    /**
     * Resets all action listeners
     */
    void resetAllActions();

    /**
     * Registers all the change listeners on WS if they're not registered already
     * @param currEvent the event in which we listen on the participant changes
     * @param updateEventCallback this is called when an Event is updated
     */
    void registerEventChangeListener(
            Event currEvent,
            Consumer<Event> updateEventCallback
    );

}
