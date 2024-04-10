package utils;

import client.utils.ServerUtils;
import commons.*;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestServerUtils implements ServerUtils {

    private final List<Event> events;
    private int counter;
    private Date lastChange;
    private final List<String> calls;
    private final Set<Integer> concurrentStatuses;
    private final List<Integer> statuses;
    private final TestWebsocket websocket;
    private boolean polled;

    /**
     * constructor
     * sets the counter for setting ids to 1 and the date to current time
     * @param websocket the websocket to use
     */
    public TestServerUtils(TestWebsocket websocket) {
        events = new ArrayList<>();
        counter = 1;
        lastChange = new Date();
        calls = new ArrayList<>();
        statuses = new ArrayList<>();
        polled = false;
        concurrentStatuses = new ConcurrentSkipListSet<>();
        this.websocket = websocket;
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
     * @return status calls made by long polling
     */
    public Set<Integer> getConcurrentStatuses() {
        return concurrentStatuses;
    }

    /**
     * @return returned statuses
     */
    public List<Integer> getStatuses() {
        return statuses;
    }

    /**
     * @return true if long polled
     */
    public boolean isPolled() {
        return polled;
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
        clone.setLastActivity(new Date());
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
        clone.setEventID(event.getId());
        event.addParticipant(clone);
        event.setLastActivity(new Date());
        if(clone != null){
            websocket.simulateAction(WebsocketActions.ADD_PARTICIPANT, clone);
        }
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
        if(participant.getName() == null || participant.getName().isEmpty()
                || !participant.getEventID().equals(eventId)) {
            statuses.add(400);
            return 400;
        }
        event.getParticipants().remove(old);
        event.addParticipant(participant);
        event.setLastActivity(new Date());
        websocket.simulateAction(WebsocketActions.UPDATE_PARTICIPANT, participant);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * deleting participant also affects expenses
     *
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
        event.getExpenses().removeIf(e -> e.getExpenseAuthor().equals(old));
        for(Expense expense : event.getExpenses()) {
            expense.getExpenseParticipants().removeIf(p -> p.equals(old));
        }
        event.getParticipants().remove(old);
        event.setLastActivity(new Date());
        websocket.simulateAction(WebsocketActions.REMOVE_PARTICIPANT, old);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * @param id id of the expense to retrieve
     * @param eventID ID of the event containing the expense
     * @return the retrieved expense, null if not found
     */
    @Override
    public Expense getExpense(long id, String eventID) {
        calls.add("getExpense");
        Event event = events.stream().filter(e -> e.getId().equals(eventID)).findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return null;
        }
        Expense expense = event.getExpenses().stream()
                .filter(e -> e.getId() == id).findAny().orElse(null);
        if(expense == null) {
            statuses.add(404);
            return null;
        }
        statuses.add(200);
        return expense.clone();
    }

    /**
     * @param expense expense to check
     * @param event event to which the expense will belong
     * @return true iff any of the mandatory fields are missing
     * or any participants in the expense are missing from the event
     */
    public boolean checkBadExpenseFields(Expense expense, Event event) {
        if(expense == null) return true;
        if(!event.getParticipants().contains(expense.getExpenseAuthor())) return true;
        if(!event.getParticipants().containsAll(expense.getExpenseParticipants())) return true;
        return expense.getCurrency() == null || expense.getCurrency().length() != 3
                || expense.getPurpose() == null || expense.getPurpose().isEmpty()
                || expense.getDate() == null;
    }

    /**
     * Makes the participants of an expense share the same instances as the participants or an event
     * so that updates to participants via updateParticipant propagate correctly
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
     * @param eventID ID of the event to which the expense belongs
     * @param expense the expense to be created
     * @return 204 for success,
     * 400 if the expense is badly formatted,
     * 404 if event is not found
     */
    @Override
    public int createExpense(String eventID, Expense expense) {
        calls.add("createExpense");
        Event event = events.stream().filter(e -> e.getId().equals(eventID)).findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return 404;
        }
        if(checkBadExpenseFields(expense, event)) {
            statuses.add(400);
            return 400;
        }
        Expense clone = expense.clone();
        clone.setId(counter++);
        clone.setEventID(eventID);
        linkExpenseParticipants(clone, event.getParticipants());
        event.addExpense(clone);
        event.setLastActivity(new Date());
        websocket.simulateAction(WebsocketActions.ADD_EXPENSE, clone);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * @param id      id of the expense to update
     * @param eventID ID of the event containing the expense
     * @param expense the updated expense object
     * @return 204 for success,
     * 400 if the expense is badly formatted,
     * 404 if event or expense is not found
     */
    @Override
    public int updateExpense(long id, String eventID, Expense expense) {
        calls.add("updateExpense");
        Event event = events.stream().filter(e -> e.getId().equals(eventID)).findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return 404;
        }
        Expense old = event.getExpenses().stream()
                .filter(e -> e.getId() == id).findAny().orElse(null);
        if(old == null) {
            statuses.add(404);
            return 404;
        }
        if(checkBadExpenseFields(expense, event) || expense.getId() != id
                || !expense.getEventID().equals(eventID)) {
            statuses.add(400);
            return 400;
        }
        Expense clone = expense.clone();
        event.getExpenses().remove(old);
        linkExpenseParticipants(clone, event.getParticipants());
        event.getExpenses().add(clone);
        event.setLastActivity(new Date());
        websocket.simulateAction(WebsocketActions.UPDATE_EXPENSE, clone);
        lastChange = new Date();
        statuses.add(204);
        return 204;
    }

    /**
     * @param id      id of the expense to delete
     * @param eventID ID of the event containing the expense
     * @return 204 for success,
     * 404 if event or expense is not found
     */
    @Override
    public int deleteExpense(long id, String eventID) {
        calls.add("deleteExpense");
        Event event = events.stream().filter(e -> e.getId().equals(eventID)).findAny().orElse(null);
        if(event == null) {
            statuses.add(404);
            return 404;
        }
        Expense old = event.getExpenses().stream()
                .filter(e -> e.getId() == id).findAny().orElse(null);
        if(old == null) {
            statuses.add(404);
            return 404;
        }
        event.getExpenses().remove(old);
        event.setLastActivity(new Date());
        websocket.simulateAction(WebsocketActions.REMOVE_EXPENSE, old);
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
//        calls.add("pollEvents"); this causes OutOfMemoryError
        polled = true;
        if(!"password".equals(inputPassword)) {
            concurrentStatuses.add(401);
            return 401;
        }
        Date started = new Date();
        long time = started.getTime();
        while(new Date().getTime() - time < timeOut) {
            try {
                if(started.before(lastChange)) {
                    concurrentStatuses.add(204);
                    return 204;
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
        concurrentStatuses.add(408);
        return 408;
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

    /**
     * Sends an API call to change an event
     * Since adding/removing participants and expenses exist, this should be used to change titles
     * @param event event to change
     * @return updated event
     */
    @Override
    public int updateEventTitle(Event event) {
        calls.add("updateEventTitle");
        if(event.getTitle() == null
                || event.getId() == null
                || event.getTitle().length() > 100
                || event.getTitle().isEmpty()){
            statuses.add(400);
            return 400;
        }
        int eventIndex = -1;
        for(int i = 0; i < events.size(); i++){
            if(events.get(i).getId().equals(event.getId())){
                eventIndex = i;
                break;
            }
        }
        if(eventIndex == -1){
            statuses.add(404);
            return 404;
        }
        websocket.simulateAction(WebsocketActions.TITLE_CHANGE, event.getTitle());
        events.add(eventIndex, event);
        statuses.add(204);
        return 204;
    }

    /**
     * test for add tag
     * @param eventID event id
     * @param tag tag
     * @return
     */
    @Override
    public int addTag(String eventID, Tag tag) {
        calls.add("addTag");
        assertTrue(true);
        return 204;
    }

    /**
     * @param eventID     event id
     * @param transaction transaction to save
     * @return status code
     */
    @Override
    public int addTransaction(String eventID, Transaction transaction) {
        calls.add("addTransaction");
        Event event = events.stream().filter(e -> e.getId().equals(eventID))
                .findFirst().orElse(null);
        if(event == null) return 404;
        if (transaction == null || !event.hasParticipant(transaction.getGiver())
                || !event.hasParticipant(transaction.getReceiver())) {
            return 400;
        }
        Transaction clone = transaction.clone(event);
        clone.setEventID(eventID);
        clone.setId(counter++);
        event.addTransaction(clone);
        event.setLastActivity(new Date());
        websocket.simulateAction(WebsocketActions.ADD_TRANSACTION, clone);
        lastChange = new Date();
        statuses.add(200);
        return 200;
    }
    /**
     * Has an up to 5% variance for different dates
     * @return mocked version of exchange rate api
     */
    @Override
    public Map<String, Double> getExchangeRates(String date){
        calls.add("getExchangeRates");
        int count = 0;
        Map<String, Double> map = new HashMap<>(Map.of(
                "USD", 1d, "EUR", 2d, "JPY", 100d, "GBP", 1.5d, "CHF", 0.9d));
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            entry.setValue(entry.getValue() *
                    (((double) Objects.hash(date, count++) / Integer.MAX_VALUE) / 20d + 1));
        }
        return map;
    }
}
