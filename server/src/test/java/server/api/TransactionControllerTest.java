package server.api;

import commons.Event;
import commons.Participant;
import commons.Transaction;
import commons.WebsocketActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import server.AdminService;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionControllerTest {
    private EventController eventController;
    private TransactionController transactionController;
    private ParticipantController participantController;
    private TestEventRepository eventRepo;
    private TestTransactionRepository transactionRepo;
    private TestParticipantRepository participantRepo;
    private TestSimpMessagingTemplate template;
    private Event event;
    private Transaction transaction;
    private Participant giver;
    private Participant receiver;

    @BeforeEach
    public void setUp() {
        TestExpenseRepository expenseRepo = new TestExpenseRepository();
        participantRepo = new TestParticipantRepository();
        eventRepo = new TestEventRepository(participantRepo, expenseRepo);
        participantRepo.setEventRepo(eventRepo);
        expenseRepo.setEventRepo(eventRepo);
        transactionRepo = new TestTransactionRepository(eventRepo);
        eventRepo.setTransactionRepo(transactionRepo);
        template = new TestSimpMessagingTemplate((message, timeout) -> false);
        TestRandom random = new TestRandom();
        AdminController adminController = new AdminController(eventRepo, new AdminService(random));
        eventController = new EventController(eventRepo, random, template, adminController);
        transactionController = new TransactionController(eventRepo, transactionRepo, template, adminController);
        participantController = new ParticipantController(participantRepo, eventRepo,
                template, adminController, expenseRepo);
        event = eventController.add(new Event("title")).getBody();
        assert event != null;
        participantController.add(new Participant("giver"), event.getId());
        giver = (Participant) template.getPayload();
        participantController.add(new Participant("receiver"), event.getId());
        receiver = (Participant) template.getPayload();
        transaction = new Transaction(giver, receiver, 10, "EUR");
    }

    @Test
    public void add() {
        var response = transactionController.add(event.getId(), transaction);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertFalse(transactionRepo.getTransactions().isEmpty());
    }

    @Test
    public void addAssignedIDs() {
        transactionController.add(event.getId(), transaction);
        Transaction shared = (Transaction) template.getPayload();
        assertEquals(event.getId(), shared.getEventID());
        assertTrue(shared.getId() > 0);
    }

    @Test
    public void addResponsesEqual() {
        var response = transactionController.add(event.getId(), transaction);
        Transaction shared = (Transaction) template.getPayload();
        assertEquals(response.getBody(), shared);
    }

    @Test
    public void addWithWrongParticipants() {
        transaction.setGiver(new Participant("impostor"));
        var response = transactionController.add(event.getId(), transaction);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void addWithNoParticipants() {
        var response = transactionController.add(event.getId(),
                new Transaction(null, null, 1, "EUR"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void addNoEvent() {
        var response = transactionController.add("AAAAA", transaction);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void addNull() {
        var response = transactionController.add(event.getId(), null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void delete() {
        Transaction added = transactionController.add(event.getId(), transaction).getBody();
        assert added != null;
        var response = transactionController.deleteById(added.getEventID(), added.getId());
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(transactionRepo.getTransactions().isEmpty());
    }

    @Test
    public void deleteFromEvent() {
        Transaction added = transactionController.add(event.getId(), transaction).getBody();
        assert added != null;
        transactionController.deleteById(added.getEventID(), added.getId());
        Event event2 = eventController.getById(event.getId()).getBody();
        assert event2 != null;
        assertTrue(event2.getTransactions().isEmpty());
    }

    @Test
    public void deleteNotFound() {
        var response = transactionController.deleteById("AAAAA", 1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteNullID() {
        var response = transactionController.deleteById(null, 1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteInvolvedParticipant() {
        Transaction added = transactionController.add(event.getId(), transaction).getBody();
        assert added != null;
        var response = participantController.deleteById(added.getGiver().getId(), event.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void deleteInvolvedParticipantWebsocket() {
        Transaction added = transactionController.add(event.getId(), transaction).getBody();
        assert added != null;
        participantController.deleteById(added.getGiver().getId(), event.getId());
        assertTrue(template.getAllHeaders().stream().map(map -> map.get("action"))
                .toList().contains(WebsocketActions.REMOVE_TRANSACTION));
    }

    @Test
    public void deleteInvolvedParticipantGoodEvent() {
        Transaction added = transactionController.add(event.getId(), transaction).getBody();
        assert added != null;
        participantController.deleteById(added.getGiver().getId(), event.getId());
        Event event2 = eventController.getById(event.getId()).getBody();
        assert event2 != null;
        assertEquals(1, event2.getParticipants().size());
        assertEquals(0, event2.getTransactions().size());
    }
}
