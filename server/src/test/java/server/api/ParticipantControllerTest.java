package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

public class ParticipantControllerTest {

    private TestEventRepository eventRepo;
    private Event event;
    private TestParticipantRepository partRepo;
    private ParticipantController partContr;
    private TestSimpMessagingTemplate template;
    @BeforeEach
    public void setup(){
        event = new Event("title");

        partRepo = new TestParticipantRepository();
        eventRepo = new TestEventRepository(partRepo);


        TestRandom random = new TestRandom();
        template = new TestSimpMessagingTemplate((message, timeout) -> false);
        EventController eventContr = new EventController(eventRepo, random, template);
        event = eventContr.add(event).getBody();
        partContr = new ParticipantController(partRepo, eventRepo, template);
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
        partContr.add(participant, event.getId());
        Participant saved = (Participant) template.getPayload();
        var actual = partContr.getById(saved.getParticipantId(), event.getId());
        assertTrue(partRepo.getCalledMethods().contains("findById"));
        assertEquals(OK, actual.getStatusCode());
        assertEquals(saved, actual.getBody());
    }

    @Test
    void addGenId() {
        Participant participant = new Participant("name");
        var saved = partContr.add(participant, event.getId());
        assertEquals(NO_CONTENT, saved.getStatusCode());
        assertTrue(partRepo.getCalledMethods().contains("save"));
        Participant actual = (Participant) template.getPayload();
        assertNotEquals(0, actual.getParticipantId());
    }
    @Test
    void addWebsocket() {
        Participant participant = new Participant("name");
        var saved = partContr.add(participant, event.getId());
        assertEquals(NO_CONTENT, saved.getStatusCode());
        assertTrue(partRepo.getCalledMethods().contains("save"));
        Participant actual = (Participant) template.getPayload();
        participant.setParticipantId(actual.getParticipantId());
        assertEquals(participant, actual);
    }

    @Test
    void EditById(){
        Participant participantNew = new Participant("new name", "new email");

        partContr.add(new Participant("old name", "old email"), event.getId());
        Participant participantOld = (Participant) template.getPayload();
        long partID = participantOld.getParticipantId();
        participantNew.setParticipantId(partID);
        assertEquals(participantOld.getName(), "old name");
        assertEquals(participantOld.getEmailAddress(), "old email");

        partContr.editParticipantById(event.getId(), partID, participantNew);
        assertTrue(partRepo.findById(partID).isPresent());
        assertEquals(participantNew, template.getPayload());

        assertTrue(partRepo.getCalledMethods().contains("save"));
        assertTrue(partRepo.getCalledMethods().contains("findById"));

        assertTrue(eventRepo.findById(event.getId()).isPresent());
        Event retrievedEvent = eventRepo.findById(event.getId()).get();

        assertTrue(retrievedEvent.hasParticipant(participantNew));

    }
    @Test
    void removeById(){
        Participant participant = new Participant("name");
        partContr.add(participant, event.getId());
        long partID = ((Participant)template.getPayload()).getParticipantId();

        assertTrue(partRepo.existsById(partID));
        assertTrue(eventRepo.findById(event.getId()).isPresent());
        assertTrue(eventRepo.findById(event.getId()).get().getParticipants().contains(participant));
        assertEquals(NO_CONTENT, partContr.deleteById(partID, event.getId()).getStatusCode());
        assertFalse(partRepo.existsById(partID));
        assertFalse(eventRepo.findById(event.getId()).get().getParticipants().contains(participant));
    }

    @Test
    void removeWebsocket() {
        Participant participant = new Participant("name");
        partContr.add(participant, event.getId());
        long partID = ((Participant)template.getPayload()).getParticipantId();
        partContr.deleteById(partID, event.getId());
        assertEquals(partID, template.getPayload());
        assertEquals("removeParticipant", template.getHeaders().get("action"));
    }
}
