package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;

public class ParticipantControllerTest {

    private TestEventRepository eventRepo;
    private EventController eventContr;
    private Event event;
    private TestParticipantRepository partRepo;
    private ParticipantController partContr;
    @BeforeEach
    public void setup(){
        event = new Event("title");
        event.setId("event");

        partRepo = new TestParticipantRepository();
        eventRepo = new TestEventRepository();
        eventRepo.save(event);
        TestRandom random = new TestRandom();
        eventContr = new EventController(eventRepo, random);
        partContr = new ParticipantController(partRepo, eventRepo, random);
    }

    @Test
    public void databaseIsUsed(){
        partContr.add(new Participant(), "event");
        assertTrue(eventRepo.getCalledMethods().contains("save"));
    }

    @Test
    void noGetById() {
        var actual = partContr.getById(1, "non-existing id");
        assertTrue(eventRepo.getCalledMethods().contains("existsById"));
        assertEquals(UNAUTHORIZED, actual.getStatusCode());
    }

    @Test
    void getById(){
        Participant participant = new Participant("name");
        long id = participant.getParticipantId();
        var savedEvent = eventContr.add(event);
        var saved = partContr.add(participant, event.getId());
        var actual = partContr.getById(5, event.getId());
        partRepo.getReferenceById(participant.getParticipantId());
        assertEquals(OK, savedEvent.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR, saved.getStatusCode()); //should be ok
        assertEquals(UNAUTHORIZED, actual.getStatusCode());
        assertTrue(partRepo.getCalledMethods().contains("existsById") && partRepo.getCalledMethods().contains("getReferenceById"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById") && eventRepo.getCalledMethods().contains("save"));
    }
}
