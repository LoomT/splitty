package utils;

import client.utils.ServerUtils;
import commons.Event;
import commons.Participant;

import java.util.ArrayList;
import java.util.List;

public class TestServerUtils implements ServerUtils {

    private final List<Event> events;
    private int counter;

    public TestServerUtils() {
        events = new ArrayList<>();
        counter = 1;
    }

    /**
     * @return all events in server
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * @param id the id of the event to get
     * @return the found event, null if not found
     */
    @Override
    public Event getEvent(String id) {
        return events.stream().filter(e -> e.getId().equals(id)).findAny().orElse(null);
    }

    /**
     * @param event the new event to be created
     * @return the created entry in the db
     */
    @Override
    public Event createEvent(Event event) {
        Event clone = event.clone();
        clone.setId(Integer.toString(counter++));
        events.add(clone);
        return clone;
    }

    /**
     * Sends a delete request for event
     *
     * @param id event id
     * @return status code
     */
    @Override
    public int deleteEvent(String id) {
        for (int i = 0; i < events.size(); i++) {
            if(events.get(i).getId().equals(id)) {
                events.remove(i);
                return 204;
            }
        }
        return 404;
    }

    /**
     * @param eventId     tbe event in which the participant should be created
     * @param participant the participant to be created
     */
    @Override
    public int createParticipant(String eventId, Participant participant) {
        Event event = events.stream().filter(e -> e.getId().equals(eventId))
                .findAny().orElse(null);
        if(event == null) return 404;
        Participant clone = participant.clone();
        clone.setId(counter++);
        event.addParticipant(clone);
        return 204;
    }

    /**
     * @param eventId     the event in which the participant should be updated
     * @param participant the participant to be updated
     */
    @Override
    public int updateParticipant(String eventId, Participant participant) {
        Event event = events.stream().filter(e -> e.getId().equals(eventId)).findAny().orElse(null);
        if(event == null) return 404;
        Participant old = event.getParticipants().stream()
                .filter(p -> p.getId() == participant.getId()).findAny().orElse(null);
        if(old == null) return 404;
        event.getParticipants().remove(old);
        event.addParticipant(participant);
        return 204;
    }

    /**
     * @param eventId       the event in which the participant should be deleted
     * @param participantId the participant to be deleted
     */
    @Override
    public int deleteParticipant(String eventId, long participantId) {
        Event event = events.stream().filter(e -> e.getId().equals(eventId)).findAny().orElse(null);
        if(event == null) return 404;
        Participant old = event.getParticipants().stream()
                .filter(p -> p.getId() == participantId).findAny().orElse(null);
        if(old == null) return 404;
        event.getParticipants().remove(old);
        return 204;
    }

    /**
     * Verify the input password
     *
     * @param inputPassword the password to verify
     * @return boolean
     */
    @Override
    public boolean verifyPassword(String inputPassword) {
        return "password".equals(inputPassword);
    }

    /**
     * Returns all events
     *
     * @param inputPassword the admin password
     * @return all events
     */
    @Override
    public List<Event> getEvents(String inputPassword) {
        if(!verifyPassword(inputPassword)) return null;
        List<Event> eventList = new ArrayList<>();
        for(Event e : events) {
            eventList.add(e.clone());
        }
        return eventList;
    }

    /**
     * Sends an API call to add the event
     * The ids of expenses and participants gets reassigned so use the returned event!
     *
     * @param password admin password
     * @param event    event to import
     * @return imported event
     */
    @Override
    public int importEvent(String password, Event event) {
        if(!verifyPassword(password)) return 401;
        events.add(event.clone());
        return 204;
    }
}
