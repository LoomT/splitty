package client.utils;

import commons.Event;
import commons.Participant;

import java.util.List;

public interface ServerUtils {

    /**
     * @param id the id of the event to get
     * @return the found event, null if not found
     */
    Event getEvent(String id);

    /**
     * @param event the new event to be created
     * @return the created entry in the db
     */
    Event createEvent(Event event);

    /**
     * Sends a delete request for event
     *
     * @param id event id
     * @return status code
     */
    int deleteEvent(String id);

    /**
     * @param eventId     tbe event in which the participant should be created
     * @param participant the participant to be created
     * @return 204 for success,
     * 400 if the participant is badly formatted,
     * 404 if event is not found
     */
    int createParticipant(String eventId, Participant participant);

    /**
     * @param eventId     the event in which the participant should be updated
     * @param participant the participant to be updated
     * @return 204 for success,
     * 400 if the participant is badly formatted,
     * 404 if event is not found
     */
    int updateParticipant(String eventId, Participant participant);

    /**
     * @param eventId       the event in which the participant should be deleted
     * @param participantId the participant to be deleted
     * @return 204 for success,
     * 400 if the participant is badly formatted,
     * 404 if event is not found
     */
    int deleteParticipant(String eventId, long participantId);

    /**
     * Verify the input password
     * @param inputPassword the password to verify
     * @return true iff password is correct
     */
    boolean verifyPassword(String inputPassword);

    /**
     * Sends an API call to server to get all events
     *
     * @param inputPassword the admin password
     * @return all events
     */
    List<Event> getEvents(String inputPassword);

    /**
     * @param inputPassword the admin password
     * @return 204 if there is a change in the database, 408 if time-outed
     */
    int pollEvents(String inputPassword);

    /**
     * Sends an API call to add the event
     * The ids of expenses and participants gets reassigned so use the returned event!
     *
     * @param password admin password
     * @param event event to import
     * @return imported event
     */
    int importEvent(String password, Event event);
}
