package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
        var actual = partContr.getById(participant.getParticipantId(), event.getId());
        partRepo.getReferenceById(participant.getParticipantId());
        assertEquals(OK, savedEvent.getStatusCode());
        assertEquals(OK, saved.getStatusCode());
        assertEquals(OK, actual.getStatusCode()); //should be ok instead of INTERNAL ERROR
        assertTrue(partRepo.getCalledMethods().contains("getReferenceById"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById") && eventRepo.getCalledMethods().contains("save"));
    }

    @Test
    void EditById(){
        Participant participantOld = new Participant("old name", "old email");
        partRepo.save(participantOld);
        partContr.add(participantOld, event.getId());
        long partID = participantOld.getParticipantId();
        Participant participantOldFromDatabase = partRepo.findById(partID).get();
        assertEquals(participantOldFromDatabase.getName(), "old name");
        assertEquals(participantOldFromDatabase.getEmailAddress(), "old email");
        partContr.editParticipantById(event.getId(), partID, "new name", "new email");
        assertTrue(partRepo.findById(partID).isPresent());
        Participant participantNew = partRepo.findById(partID).get();
        assertTrue(partRepo.getCalledMethods().contains("save"));
        assertTrue(partRepo.getCalledMethods().contains("findById"));
        assertTrue(partRepo.getCalledMethods().contains("existsById"));
        assertTrue(partRepo.getCalledMethods().contains("getReferenceById"));
        assertEquals(participantNew.getName(), "new name");
        assertEquals(participantNew.getEmailAddress(), "new email");
    }

    @Test
    void removeById(){
        Participant participant = new Participant("name");
        partRepo.save(participant);
        long partID = participant.getParticipantId();
        partContr.add(participant, event.getId());
        assertTrue(partRepo.findById(partID).isPresent());
        assertTrue(eventRepo.findById(event.getId()).isPresent());
        assertTrue(eventRepo.findById(event.getId()).get().getParticipants().contains(participant));
        assertTrue(partRepo.existsById(partID));
        partContr.deleteById(partID, event.getId());
        assertFalse(partRepo.existsById(partID));
    }
}
