package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

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
        eventRepo = new TestEventRepository(partRepo);

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
        assertEquals(OK, actual.getStatusCode());
        assertTrue(partRepo.getCalledMethods().contains("getReferenceById"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById") && eventRepo.getCalledMethods().contains("save"));
    }

    @Test
    void EditById(){
        Participant participantOld = new Participant("old name", "old email");
        Participant participantNew = new Participant("new name", "new email");

        partRepo.save(participantOld);
        partContr.add(participantOld, event.getId());
        long partID = participantOld.getParticipantId();
        Participant participantOldFromDatabase = partRepo.findById(partID).get();

        assertEquals(participantOldFromDatabase.getName(), "old name");
        assertEquals(participantOldFromDatabase.getEmailAddress(), "old email");

        partContr.editParticipantById(event.getId(), partID, participantNew);
        assertTrue(partRepo.findById(partID).isPresent());
        Participant participantActual = partRepo.findById(partID).get();

        assertTrue(partRepo.getCalledMethods().contains("save"));
        assertTrue(partRepo.getCalledMethods().contains("findById"));

        assertTrue(eventRepo.findById(event.getId()).isPresent());
        Event retrievedEvent = eventRepo.findById(event.getId()).get();

        assertTrue(retrievedEvent.hasParticipant(participantNew));
        assertFalse(retrievedEvent.hasParticipant(participantOld));
        assertEquals("new name", participantActual.getName());
        assertEquals("new email", participantActual.getEmailAddress());
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
        assertEquals(NO_CONTENT, partContr.deleteById(partID, event.getId()).getStatusCode());
        assertFalse(partRepo.existsById(partID));
        assertFalse(eventRepo.findById(event.getId()).get().getParticipants().contains(participant));
    }

    @Test
    void invalidRequestEvent(){
        Participant participant = new Participant("name", null);
        String eventIDNotExist = "event";
        var response = partContr.add(participant, eventIDNotExist);
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addParticipantNull(){
        Event exist = new Event("title");
        String existID = "words";
        exist.setId(existID);
        eventRepo.save(exist);
        var response = partContr.add(null, existID);
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addParticipantNameNull(){
        Event event = new Event("title");
        event.setId("id");
        Participant participant = new Participant();
        eventRepo.save(event);
        var response = partContr.add(participant, "id");
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addParticipantNameEmpty(){
        Event event = new Event("title");
        event.setId("id");
        Participant participant = new Participant("");
        eventRepo.save(event);
        var response = partContr.add(participant, "id");
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editParticipantNonExisting(){
        Event exist = new Event("title");
        String existID = "words";
        exist.setId(existID);
        eventRepo.save(exist);
        Participant validParticipant = new Participant("name");
        var response = partContr.editParticipantById(existID, 0, validParticipant);
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    void editParticipantInvalid(){
        Event exist = new Event("title");
        String existID = "words";
        Participant existingParticipant = new Participant("name");
        exist.addParticipant(existingParticipant);
        exist.setId(existID);
        eventRepo.save(exist);
        Participant participantNameNull = new Participant();
        Participant participantNameEmpty = new Participant("");
        var responseNull = partContr.editParticipantById(existID, 2, null);
        var responseNameNull = partContr.editParticipantById(existID, 2, participantNameNull);
        var responseNameEmpty = partContr.editParticipantById(existID, 2, participantNameEmpty);
        assertEquals(BAD_REQUEST, responseNull.getStatusCode());
        assertEquals(BAD_REQUEST, responseNameNull.getStatusCode());
        assertEquals(BAD_REQUEST, responseNameEmpty.getStatusCode());
    }
    @Test
    void deleteParticipantNonExisting(){
        Event exist = new Event("title");
        String existID = "words";
        exist.setId(existID);
        eventRepo.save(exist);
        var response = partContr.deleteById(0, existID);
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    void unauthorizedGetById(){
        Event event1 = new Event("title");
        Event event2 = new Event("title");
        event1.setId("id1");
        event2.setId("id2");
        Participant participant = new Participant("name");
        event1.addParticipant(participant);
        eventRepo.save(event1);
        eventRepo.save(event2);
        var savedSuccess = partContr.add(participant, "BCDEF");
        var getByID401 = partContr.getById(2, "id2");
        assertEquals(NO_CONTENT, savedSuccess.getStatusCode());
        assertEquals(UNAUTHORIZED, getByID401.getStatusCode());
    }

    @Test
    void unauthorizedEditById(){
        Event event1 = new Event("title");
        Event event2 = new Event("title");
        event1.setId("id1");
        event2.setId("id2");
        Participant participant = new Participant("old name");
        Participant editedParticipant = new Participant("new name");
        editedParticipant.setParticipantId(2);
        event1.addParticipant(participant);
        eventRepo.save(event1);
        eventRepo.save(event2);
        var saved = partContr.add(participant, "BCDEF");
        var editByID401 = partContr.editParticipantById("id2", 2, editedParticipant);
        assertEquals(NO_CONTENT, saved.getStatusCode());
        assertEquals(UNAUTHORIZED, editByID401.getStatusCode());
    }

    @Test
    void unauthorizedDeleteById(){
        Event event1 = new Event("title");
        Event event2 = new Event("title");
        event1.setId("id1");
        event2.setId("id2");
        Participant participant = new Participant("old name");
        event1.addParticipant(participant);
        eventRepo.save(event1);
        eventRepo.save(event2);
        var saved = partContr.add(participant, "BCDEF");
        var editByID401 = partContr.deleteById( 2, "id2");
        assertEquals(NO_CONTENT, saved.getStatusCode());
        assertEquals(UNAUTHORIZED, editByID401.getStatusCode());
    }

    @Test
    void removeWebsocket() {
        Participant participant = new Participant("name");
        partContr.add(participant, event.getId());
        long partID = ((Participant)template.getPayload()).getParticipantId();
        partContr.deleteById(partID, event.getId());
        assertFalse(partRepo.existsById(partID));
    }
}
