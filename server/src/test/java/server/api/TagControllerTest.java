package server.api;

import commons.Event;
import commons.EventWeakKey;
import commons.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.AdminService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class TagControllerTest {
    private TagController controller;
    private TestTagRepository tagRepo;

    @BeforeEach
    public void setUp() {
        TestEventRepository eventRepo = new TestEventRepository();
        TestTagRepository tagRepo = new TestTagRepository(eventRepo);
        controller = new TagController(eventRepo, tagRepo, new TestSimpMessagingTemplate((message, timeout) -> false), new AdminController(eventRepo, new AdminService(new TestRandom()))
                );
        this.tagRepo=tagRepo;
        Event event = new Event();
        event.setId("eventId");
        eventRepo.save(event);
    }

    @Test
    void simpleAddTagTest(){
        Tag tag = new Tag("name", "blue");
        controller.addTag("eventId", tag);
        Optional<Tag> test = tagRepo.findById(new EventWeakKey("eventId", 1));
        assertTrue(test.isPresent());
        assertEquals(tag, test.get());
    }

    @Test
    void InvalidEventAddTagTest(){
        Tag tag = new Tag("name", "blue");
        var actual = controller.addTag("notArealID", tag);
        Optional<Tag> test = tagRepo.findById(new EventWeakKey("eventId", 1));
        assertFalse(test.isPresent());
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void InvalidTagAddTagTest(){
        var actual = controller.addTag("eventId", null);
        Optional<Tag> test = tagRepo.findById(new EventWeakKey("eventId", 1));
        assertFalse(test.isPresent());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }
}
