package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        partContr = new ParticipantController(partRepo, eventRepo);
    }

    @Test
    public void databaseIsUsed(){
        partContr.add(new Participant("name"), event.getId());
        assertTrue(eventRepo.getCalledMethods().contains("save"));
    }

    @Test
    void noGetById() {
        var actual = partContr.getById(1, "non-existing id");
        assertTrue(eventRepo.getCalledMethods().contains("findById"));
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    void getById(){
        Participant participant = new Participant("name");
        partRepo.save(participant);
        var savedEvent = eventContr.add(event);
        var saved = partContr.add(participant, event.getId());
        long tst = participant.getParticipantId();
        List<Event> list = eventRepo.findAll();
        List<Participant> listo = partRepo.findAll();
        String test = event.getId();
        long id = participant.getParticipantId();
        var actual = partContr.getById(participant.getParticipantId(), event.getId());
        partRepo.getReferenceById(participant.getParticipantId());
        assertEquals(OK, savedEvent.getStatusCode());
        assertEquals(OK, saved.getStatusCode());
        assertEquals(OK, actual.getStatusCode()); //should be ok instead of INTERNAL ERROR
        assertTrue(partRepo.getCalledMethods().contains("getReferenceById"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById") && eventRepo.getCalledMethods().contains("save"));
    }
}
