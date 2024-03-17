package server.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import commons.WebsocketActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Expense;
import commons.Participant;
import commons.Event;

import java.util.*;


public class ExpenseControllerTest {
    private TestEventRepository eventRepo;
    private EventController eventContr;
    private TestExpenseRepository repoExpense;
    private ExpenseController expenseContr;
    private TestParticipantRepository partRepo;
    private Expense expense;
    private Event event;
    private Participant p1, p2, expAuth;

    List<Participant> expPart;
    private TestSimpMessagingTemplate template;

    @BeforeEach
    void setup() {
        TestRandom random = new TestRandom();
        expPart = new ArrayList<>();
        repoExpense = new TestExpenseRepository();
        partRepo = new TestParticipantRepository();
        eventRepo = new TestEventRepository();
        template = new TestSimpMessagingTemplate((message, timeout) -> false);
        expenseContr = new ExpenseController(repoExpense, eventRepo, template);
        eventContr = new EventController(eventRepo, random, template);

        // Creating sample participants
        p1 = new Participant("Mihai");
        p2 = new Participant("Alex");
        expAuth = new Participant("Rares");
        expPart.add(p1);
        expPart.add(p2);
        expPart.add(expAuth);
        expense = new Expense(p2, "Groceries", 20, "USD", expPart, "Club");
        List<Expense> temp = new ArrayList<>();
        temp.add(expense);
        event = new Event("title", expPart, temp);
        event.setId("10");
        eventRepo.save(event);
        eventContr.add(event);
        expense.setEventId(event.getId());
    }

    @Test
    public void testDatabaseIsUsed() {
        expenseContr.addExpense(expense, event.getId());
        assertTrue(repoExpense.getCalledMethods().contains("save"));
        assertTrue(eventRepo.getCalledMethods().contains("findById"));
        assertTrue(eventRepo.getCalledMethods().contains("save"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById"));
    }

    @Test
    public void testNoGetById() {
        var actual = expenseContr.getById(5, event.getId());
        assertTrue(repoExpense.getCalledMethods().contains("findById"));
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testGetByIdUnauthorized() {
        Event ev = new Event("some title");
        ev.setId("10");
        eventRepo.save(ev);
        eventContr.add(ev);
        Expense anotherExpense = new Expense();
        anotherExpense.setExpenseID(5);
        repoExpense.save(anotherExpense);
        var actual = expenseContr.getById(anotherExpense.getExpenseID(), ev.getId());

        assertEquals(UNAUTHORIZED, actual.getStatusCode());
    }

    @Test
    public void testGetByIdAuthorized() {
        event.setId("10");
        eventRepo.save(event);
        eventContr.add(event);

        expense.setExpenseID(5);
        expense.setEventId(event.getId());
        repoExpense.save(expense);
        var actual = expenseContr.getById(expense.getExpenseID(), event.getId());

        assertEquals(OK, actual.getStatusCode());
    }

    @Test
    public void testAddBadRequest1() {
        Expense exp = new Expense();
        Event ev = new Event("some title");
        ev.setId("10");
        var actual = expenseContr.addExpense(exp, ev.getId());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void testAddBadRequest2() {
        expense.setExpenseID(3);
        Event ev = new Event();
        var actual = expenseContr.addExpense(expense, ev.getId());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void testAdd() {
        var actual = expenseContr.addExpense(expense, event.getId());
        assertTrue(eventRepo.getCalledMethods().contains("findById"));
        assertTrue(eventRepo.getCalledMethods().contains("save"));
        assertTrue(repoExpense.getCalledMethods().contains("save"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById"));
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testAddWebsocket() {
        var actual = expenseContr.addExpense(expense, event.getId());
        assertTrue(eventRepo.getCalledMethods().contains("findById"));
        assertTrue(eventRepo.getCalledMethods().contains("save"));
        assertTrue(repoExpense.getCalledMethods().contains("save"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById"));
        assertEquals(NO_CONTENT, actual.getStatusCode());
        Expense saved = (Expense) template.getPayload();
        expense.setExpenseID(saved.getExpenseID());
        assertEquals(expense, saved);
    }

    @Test
    public void testDeleteNothing() {
        Expense exp = new Expense();
        repoExpense.save(exp);
        var actual = expenseContr.deleteById(24, "10");
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteById() {
        expenseContr.addExpense(expense, event.getId());
        long expenseID = ((Expense)template.getPayload()).getExpenseID();

        assertTrue(repoExpense.existsById(expenseID));

        assertTrue(eventRepo.findById(event.getId()).isPresent());
        assertTrue(eventRepo.findById(event.getId()).get().getExpenses().contains(expense));
        assertEquals(NO_CONTENT, expenseContr.deleteById(expenseID, event.getId()).getStatusCode());
        assertFalse(partRepo.existsById(expenseID));

    }

    @Test
    public void testDeleteWebsocket() {
        expenseContr.addExpense(expense, event.getId());
        long id = ((Expense)template.getPayload()).getExpenseID();
        expenseContr.deleteById(id, event.getId());
        assertEquals(id, template.getPayload());
        assertEquals(WebsocketActions.REMOVE_EXPENSE, template.getHeaders().get("action"));
    }

    @Test
    public void testUpdateExpense() {
        Expense exp = new Expense();
        var updated = expenseContr.updateExpense(26, exp, event.getId());
        assertEquals(BAD_REQUEST, updated.getStatusCode());
    }
}
