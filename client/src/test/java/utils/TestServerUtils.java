package utils;

import client.utils.ServerUtils;
import commons.Event;
import commons.Participant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestServerUtils implements ServerUtils {

    private final List<Event> events;
    private int counter;
    private Date lastChange;
    private final List<String> calls;
    private final List<Integer> statuses;

    public TestServerUtils() {
        events = new ArrayList<>();
        counter = 1;
        lastChange = new Date();
        calls = new ArrayList<>();
        statuses = new ArrayList<>();
    }

    /**
     * Returns all events for testing purposes
     *
     * @return all events in server
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * @return calls
     */
    public List<String> getCalls() {
        return calls;
    }

    /**
     * @return returned statuses
     */
    public List<Integer> getStatuses() {
        return statuses;
    }

    /**
     * @param id the id of the event to get
     * @return the found event, null if not found
     */
    @Override
    public Event getEvent(String id) {
        calls.add("getEvent");
        Event event = events.stream().filter(e -> e.getId().equals(id)).findAny().orElse(null);
        if(event == null) statuses.add(404);
        else statuses.add(200);
        return event;
    }

    /**
     * @param event the new event to be created
     * @return the created entry in the db, null if error
     */
    @Override
    public Event createEvent(Event event) {
        calls.add("createEvent");
        if(event.getTitle() == null || event.getTitle().isEmpty()) {
            statuses.add(400);
            return null;
        }
        Event clone = event.clone();
        clone.setId(Integer.toString(counter++));
        events.add(clone);
        lastChange = new Date();
        statuses.add(200);
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
        calls.add("deleteEvent");
        for (int i = 0; i < events.size(); i++) {
            if(events.get(i).getId().equals(id)) {
                events.remove(i);
                lastChange = new Date();
                statuses.add(204);
                return 204;
            }
        }
        statuses.add(404);
        return 404;
    }

    /**
     * @param eventId     tbe event in which the participant should be created
     * @param participant the participant to be created
     */
    @Override
    public int createParticipant(String eventId, Participant participant) {
        calls.add("createParticipant");
        Event event = events.stream().filter(e -> e.getId().equals(eventId))
                .findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return 404;
        }
        if(participant.getName() == null || participant.getName().isEmpty()) {
            statuses.add(400);
            return 400;
        }
        Participant clone = participant.clone();
        clone.setId(counter++);
        event.addParticipant(clone);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * @param eventId     the event in which the participant should be updated
     * @param participant the participant to be updated
     */
    @Override
    public int updateParticipant(String eventId, Participant participant) {
        calls.add("updateParticipant");
        Event event = events.stream().filter(e -> e.getId().equals(eventId)).findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return 404;
        }
        Participant old = event.getParticipants().stream()
                .filter(p -> p.getId() == participant.getId()).findAny().orElse(null);
        if(old == null) {
            statuses.add(404);
            return 404;
        }
        if(participant.getName() == null || participant.getName().isEmpty()) {
            statuses.add(400);
            return 400;
        }
        event.getParticipants().remove(old);
        event.addParticipant(participant);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * @param eventId       the event in which the participant should be deleted
     * @param participantId the participant to be deleted
     */
    @Override
    public int deleteParticipant(String eventId, long participantId) {
        calls.add("deleteParticipant");
        Event event = events.stream().filter(e -> e.getId().equals(eventId)).findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return 404;
        }
        Participant old = event.getParticipants().stream()
                .filter(p -> p.getId() == participantId).findAny().orElse(null);
        if(old == null) {
            statuses.add(404);
            return 404;
        }
        event.getParticipants().remove(old);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * Verify the input password
     *
     * @param inputPassword the password to verify
     * @return true iff password is "password"
     */
    @Override
    public boolean verifyPassword(String inputPassword) {
        calls.add("verifyPassword");
        if("password".equals(inputPassword)) {
            statuses.add(200);
            return true;
        } else {
            statuses.add(401);
            return false;
        }
    }

    /**
     * Returns all events
     *
     * @param inputPassword the admin password
     * @return all events
     */
    @Override
    public List<Event> getEvents(String inputPassword) {
        calls.add("getEvents");
        if(!"password".equals(inputPassword)) {
            statuses.add(401);
            return null;
        }
        List<Event> eventList = new ArrayList<>();
        for(Event e : events) {
            eventList.add(e.clone());
        }
        statuses.add(200);
        return eventList;
    }

    /**
     * @param inputPassword the admin password
     * @param timeOut time in ms until server sends a time-out signal
     * @return 204 if there is a change in the database, 408 if time-outed
     */
    @Override
    public int pollEvents(String inputPassword, Long timeOut) {
        calls.add("pollEvents");
        if(!"password".equals(inputPassword)) {
            statuses.add(401);
            return 401;
        }
        Date now = new Date();
        try {
            Thread.sleep(timeOut);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        statuses.add(now.before(lastChange) ? 204 : 408);
        return now.before(lastChange) ? 204 : 408;
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
        calls.add("importEvent");
        if(!verifyPassword(password)) {
            statuses.add(401);
            return 401;
        }
        events.add(event.clone());
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }
}
