package server.api;

import commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;


public class ExpenseControllerTest {
    private TestEventRepository eventRepo;
    private EventController eventContr;
    private TestExpenseRepository repoExpense;
    private ExpenseController expenseContr;
    private Expense expense, expense2, updExp;
    private Event event;
    private Participant p1, p2, expAuth;

    List<Participant> expPart;
    private TestSimpMessagingTemplate template;

    @BeforeEach
    void setup() {
        TestRandom random = new TestRandom();
        expPart = new ArrayList<>();
        repoExpense = new TestExpenseRepository();
        eventRepo = new TestEventRepository(repoExpense);
        repoExpense.setEventRepo(eventRepo);
        template = new TestSimpMessagingTemplate((message, timeout) -> false);
        expenseContr = new ExpenseController(repoExpense, template);
        eventContr = new EventController(eventRepo, random, template);

        // Creating sample participants
        p1 = new Participant("Mihai");
        p2 = new Participant("Alex");
        expAuth = new Participant("Rares");
        expPart.add(p1);
        expPart.add(p2);
        expPart.add(expAuth);
        expense = new Expense(p2, "Groceries", 20, "USD", expPart, "Club");
        expense2 = new Expense(p1, "Drinks", 10, "EUR", expPart, "Out");
        updExp = new Expense(p2, "Drinks", 30, "EUR", expPart, "Out");
        List<Expense> temp = new ArrayList<>();
        event = new Event("title", expPart, temp);
        var added = eventContr.add(event);
        event = added.getBody();
        expenseContr.addExpense(expense, event.getId());
    }

    @Test
    public void testDatabaseIsUsed() {
        expenseContr.addExpense(expense2, event.getId());
        assertTrue(repoExpense.getCalledMethods().contains("save"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById"));
    }

    @Test
    public void testNoGetById() {
        var actual = expenseContr.getById(5, event.getId());
        assertTrue(repoExpense.getCalledMethods().contains("findById"));
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testGetByWrongEventId() {
        expenseContr.addExpense(expense2, event.getId());
        long id = ((Expense) template.getPayload()).getId();
        var actual = expenseContr.getById(id, "fakeID");

        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testGetByIdAuthorized() {
        var actual = expenseContr.getById(expense.getId(), event.getId());
        assertEquals(OK, actual.getStatusCode());
    }

    @Test
    public void testAddBadRequest1() {
        Expense exp = new Expense();
        var actual = expenseContr.addExpense(exp, event.getId());
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }
    @Test
    public void testAdd() {
        //work with new expense, expense2
        var actual = expenseContr.addExpense(expense2, event.getId());
        assertTrue(repoExpense.getCalledMethods().contains("save"));
        assertTrue(eventRepo.getCalledMethods().contains("existsById"));
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testAddWebsocket() {
        expenseContr.addExpense(expense2, event.getId());
        Expense saved = (Expense) template.getPayload();
        expense.setId(saved.getId());
        assertEquals(expense2, saved);
    }

    @Test
    public void testDeleteNothing() {
        var actual = expenseContr.deleteById(24, event.getId());
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteById() {
        expenseContr.addExpense(expense2, event.getId());
        long expenseID = ((Expense)template.getPayload()).getId();

        assertTrue(repoExpense.existsById(new EventWeakKey(event.getId(), expenseID)));
        assertTrue(eventRepo.findById(event.getId()).isPresent());
        assertTrue(eventRepo.findById(event.getId()).get().getExpenses().contains(expense2));
        assertEquals(NO_CONTENT, expenseContr.deleteById(expenseID, event.getId()).getStatusCode());
        assertFalse(repoExpense.existsById(new EventWeakKey(event.getId(), expenseID)));

    }

    @Test
    public void testDeleteWebsocket() {
        expenseContr.addExpense(expense2, event.getId());
        long id = ((Expense)template.getPayload()).getId();
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

    @Test
    public void testUpdateExpenseSuccessful() {
        expenseContr.addExpense(expense2, event.getId());
        var updated = expenseContr.updateExpense(expense2.getId(), expense2, event.getId());
        assertEquals(NO_CONTENT, updated.getStatusCode());
    }

    @Test
    public void testUpdateWebsocket() {
        expenseContr.addExpense(expense2, event.getId());
        long expenseID = ((Expense)template.getPayload()).getId();
        updExp.setId(expenseID);
        updExp.setEventID(event.getId());
        var actual = expenseContr.updateExpense(expenseID, updExp, event.getId());

        assertEquals(NO_CONTENT, actual.getStatusCode());
        assertEquals(WebsocketActions.UPDATE_EXPENSE, template.getHeaders().get("action"));
        assertEquals(updExp, template.getPayload());
    }



}
