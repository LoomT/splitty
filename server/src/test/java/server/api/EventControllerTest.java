package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

class EventControllerTest {
    private TestEventRepository repo;
    private EventController sut;
    private TestRandom random;
    @BeforeEach
    void setUp() {
        repo = new TestEventRepository();
        sut = new EventController(repo, random);
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
        assertEquals(p, actual.getBody().getParticipants().getFirst());
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
    void changeTitleById() {
        var added = sut.add(new Event("title"));
        String id = Objects.requireNonNull(added.getBody()).getId();
        var actual = sut.changeTitleById(id, "new title");
        assertEquals("new title", Objects.requireNonNull(actual.getBody()).getTitle());
    }
}