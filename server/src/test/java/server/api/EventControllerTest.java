package server.api;

import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.WebsocketActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.AdminService;

import java.util.Date;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

class EventControllerTest {
    private TestEventRepository repo;
    private EventController sut;

    private TestSimpMessagingTemplate template;

    @BeforeEach
    void setUp() {
        TestRandom random = new TestRandom();
        repo = new TestEventRepository();
        template = new TestSimpMessagingTemplate((message, timeout) -> false);
        AdminController adminController = new AdminController(repo, new AdminService(random));
        sut = new EventController(repo, random, template, adminController);
    }
    @Test
    public void databaseIsUsed() {
        sut.add(new Event("title"));
        assertTrue(repo.getCalledMethods().contains("save"));
    }
    @Test
    void noGetById() {
        var actual = sut.getById("a");
        assertTrue(repo.getCalledMethods().contains("findById"));
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void nestedEntities() {
        Event event = new Event("test");
        Participant p = new Participant("Bob");
        event.addParticipant(p);
        var actual = sut.add(event);
        assertEquals(p, Objects.requireNonNull(actual.getBody()).getParticipants().getFirst());
    }

    @Test
    void getById() {
        Event e = new Event("test");
        var saved = sut.add(e);
        var actual = sut.getById(Objects.requireNonNull(saved.getBody()).getId());
        assertTrue(repo.getCalledMethods().contains("findById"));
        assertEquals(OK, actual.getStatusCode());
        assertEquals(e.getTitle(), Objects.requireNonNull(actual.getBody()).getTitle());
    }

    @Test
    void add() {
        var actual = sut.add(new Event("title"));
        assertTrue(repo.getCalledMethods().contains("save"));
        assertEquals(OK, actual.getStatusCode());
        assertNotNull(actual.getBody()); // check that body is not null
    }

    @Test
    void cannotAddEventWithNullTitle() {
        var actual = sut.add(new Event(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void cannotAddEventWithEmptyTitle() {
        var actual = sut.add(new Event(""));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void cannotAddNull() {
        var actual = sut.add(null);
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void deleteNothing() {
        var actual = sut.deleteById("a");
        assertTrue(repo.getCalledMethods().contains("existsById"));
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void delete() {
        var added = sut.add(new Event("title"));
        var actual = sut.deleteById(Objects.requireNonNull(added.getBody()).getId());
        assertTrue(repo.getCalledMethods().contains("deleteById"));
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void deleteWebsocketDestination() {
        var added = sut.add(new Event("title"));
        String id = added.getBody().getId();
        sut.deleteById(id);
        assertEquals("/event/" + id, template.getDestination());
    }

    @Test
    void deleteWebsocketPayload() {
        var added = sut.add(new Event("title"));
        String id = added.getBody().getId();
        sut.deleteById(id);
        assertEquals("delete", template.getPayload());
    }

    @Test
    void deleteWebsocketHeaders() {
        var added = sut.add(new Event("title"));
        String id = added.getBody().getId();
        sut.deleteById(id);
        assertTrue(template.getHeaders().containsKey("action"));
        assertEquals(WebsocketActions.DELETE_EVENT, template.getHeaders().get("action"));
        assertTrue(template.getHeaders().containsKey("type"));
        assertEquals("java.lang.String", template.getHeaders().get("type"));
    }

    @Test
    void changeTitleById() {
        var added = sut.add(new Event("title"));
        String id = Objects.requireNonNull(added.getBody()).getId();
        var actual = sut.changeTitleById(id, "new title");
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void changeTitleByIdWebsocket() {
        var added = sut.add(new Event("title"));
        Event event = added.getBody();
        String id = event.getId();
        sut.changeTitleById(id, "new title");
        assertEquals("/event/" + id, template.getDestination());
        assertEquals(WebsocketActions.TITLE_CHANGE, template.getHeaders().get("action"));
        assertEquals("new title", template.getPayload());
    }

    @Test
    void randomId() {
        var added = sut.add(new Event("title"));
        assertEquals("ZABCD", Objects.requireNonNull(added.getBody()).getId());
    }

    @Test
    void activityDateAfterAddingEvent() {
        Date before = new Date();
        Event event = new Event("title");
        Event added = sut.add(event).getBody();

        assert added != null;
        assertNotNull(added.getLastActivity());
        assertTrue(added.getLastActivity().compareTo(before) >= 0);
        assertTrue(added.getLastActivity().compareTo(new Date()) <= 0);
    }

    @Test
    void activityUpdateAfterChangingTitle() {
        Event event = new Event("title");
        Event added = sut.add(event).getBody();
        assert added != null;
        Date before = added.getLastActivity();
        String id = added.getId();
        sut.changeTitleById(id, "new title");
        Event updated = sut.getById(id).getBody();
        assert updated != null;
        assertTrue(updated.getLastActivity().compareTo(before) >= 0);
        assertTrue(updated.getLastActivity().compareTo(new Date()) <= 0);
    }

    @Test
    void changeEvent(){
        Event event = new Event("title");
        Event added = sut.add(event).getBody();
        assert added != null;
        event.addParticipant(new Participant());
        event.addExpense(new Expense());
        event.setTitle("new title");
        sut.changeEvent(event.getId(), event);
        assertEquals("/event/" + event.getId(), template.getDestination());
        assertEquals(WebsocketActions.TITLE_CHANGE, template.getHeaders().get("action"));
        assertEquals("new title", template.getPayload());
    }

    @Test
    void changeEventWithIllegalEvent(){
        Event event = new Event("title");
        Event added = sut.add(event).getBody();
        assert added != null;
        event.setId("Illegal ID");
        var actual = sut.changeEvent(event.getId(), event);
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void changeEventNotExist(){
        Event event = new Event("title");
        var actual = sut.changeEvent(event.getId(), event);
        assertEquals(BAD_REQUEST, actual.getStatusCode());
        Event added = sut.add(event).getBody();
        assert added != null;
        Event test = new Event("test");
        test.setId("QQQQQ");
        actual = sut.changeEvent("QQQQQ", test);
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }
}