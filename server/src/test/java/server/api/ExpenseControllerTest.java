package server.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import commons.Expense;
import commons.Participant;

import java.util.*;


public class ExpenseControllerTest {
    private TestExpenseRepository expenseRepo;
    private ExpenseController expenseContr;
    private TestParticipantRepository partRepo;
    private ParticipantController partContr;
    private Expense expense;

    Expense e1;
    Participant p1, p2, expAuth;
    List<Participant> expPart = new ArrayList<>();

    @BeforeEach
    void setup() {
        p1 = new Participant();
        p2 = new Participant();
        expAuth = new Participant();
        expPart.add(p1);
        expPart.add(p2);
        expPart.add(expAuth);

        e1 = new Expense(p1, "Groceries", 50.0, "EUR", expPart, "Food");
        e1.setExpenseID(10);

        expenseRepo = new TestExpenseRepository();
        partRepo = new TestParticipantRepository();
        expenseRepo.save(e1);

        expenseContr = new ExpenseController(expenseRepo, partRepo);

    }

    @Test
    public void testDatabaseIsUsed() {
        expenseContr.addExpense(e1);
        assertTrue(expenseRepo.getCalledMethods().contains("save"));
    }

    @Test
    public void testNoGetById() {
        var actual = expenseContr.getById(5);
        assertTrue(expenseRepo.getCalledMethods().contains("findById"));
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testGetById() {
        Expense exp = new Expense(p2, "Groceries", 20, "USD", expPart, "Club");
        exp.setExpenseID(3);

        expenseRepo.save(exp);
        partRepo.save(p2);

        Participant participant = new Participant();
        partRepo.save(participant);

        var saved = expenseContr.addExpense(exp);
        System.out.println(saved.getBody());
        var actual = expenseContr.getById(Objects.requireNonNull(saved.getBody()).getExpenseID());
        expenseRepo.getReferenceById(exp.getExpenseID());

        Set<Expense> temp = new HashSet<>();
        temp.add(exp);
        p2.setAuthoredExpenseSet(temp);
        assertTrue(partRepo.getCalledMethods().contains("save"));
        assertTrue(p2.getAuthoredExpenseSet().contains(exp));
        assertEquals(OK, actual.getStatusCode());
        assertTrue(expenseRepo.getCalledMethods().contains("getReferenceById"));

    }

    @Test
    public void testAdd() {
        Expense exp = new Expense(p2, "Groceries", 20, "USD", expPart, "Club");
        exp.setExpenseID(3);
        var actual = expenseContr.addExpense(exp);
        assertTrue(expenseRepo.getCalledMethods().contains("save"));
        assertEquals(OK, actual.getStatusCode());
        assertNotNull(actual.getBody());

    }

    @Test
    public void testCantAddEventWithNullAuthor() {
        Expense exp = new Expense(null, "Groceries", 20, "USD", expPart, "Club");
        exp.setExpenseID(3);

        var actual = expenseContr.addExpense(exp);
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteNothing() {
        Expense exp = new Expense(null, "Groceries", 20, "USD", expPart, "Club");
        exp.setExpenseID(9);
        var added = expenseContr.addExpense(exp);
        var actual = expenseContr.deleteById(24);
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteById() {
        Expense exp = new Expense(null, "Groceries", 20, "USD", expPart, "Club");
        exp.setExpenseID(9);
        var added = expenseContr.addExpense(exp);
        var actual = expenseContr.deleteById(9);
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testUpdateExpense() {
        Expense exp = new Expense(null, "Groceries", 20, "USD", expPart, "Club");
        exp.setExpenseID(9);
        var updated = expenseContr.updateExpense(26, exp);
        assertEquals(26, updated.getBody().getExpenseID());
        assertEquals(OK, updated.getStatusCode());
    }



}
