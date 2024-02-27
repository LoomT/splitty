package server.api;

import commons.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

class EventControllerTest {

    private TestEventRepository repo;
    private EventController sut;
    @BeforeEach
    void setUp() {
        repo = new TestEventRepository();
        sut = new EventController(repo);
    }

    @Test
    public void databaseIsUsed() {
        sut.add(new Event("title"));
        assertTrue(repo.getCalledMethods().contains("save"));
    }
    @Test
    void findById() {

    }

    @Test
    void add() {
        var actual = sut.add(new Event("title"));
        assertEquals(OK, actual.getStatusCode());
    }

    @Test
    public void cannotAddEventWithNullTitle() {
        var actual = sut.add(new Event(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void cannotAddEventWithEmptyTitle() {
        var actual = sut.add(new Event(""));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }
}