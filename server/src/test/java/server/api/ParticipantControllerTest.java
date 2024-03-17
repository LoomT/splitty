package server.api;

import commons.Event;
import commons.Participant;
import commons.WebsocketActions;
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
        assertEquals(partID, template.getPayload());
        assertEquals(WebsocketActions.REMOVE_PARTICIPANT, template.getHeaders().get("action"));
    }
}
