package server.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

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
    private Participant p1, p2, expAuth;

    List<Participant> expPart;

    @BeforeEach
    void setup() {
        expPart = new ArrayList<>();
        expenseRepo = new TestExpenseRepository();
        partRepo = new TestParticipantRepository();
        expenseContr = new ExpenseController(expenseRepo, partRepo);

        // Creating sample participants
        p1 = new Participant("Mihai");
        p2 = new Participant("Alex");
        expAuth = new Participant("Rares");
        expPart.add(p1);
        expPart.add(p2);
        expPart.add(expAuth);
        expense = new Expense(p2, "Groceries", 20, "USD", expPart, "Club");
    }

    @Test
    public void testDatabaseIsUsed() {
        expenseContr.addExpense(expense);
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
        expenseRepo.save(expense);
        partRepo.save(expense.getExpenseAuthor());

        var saved = expenseContr.addExpense(expense);
        var actual = expenseContr.getById(Objects.requireNonNull(saved.getBody()).getExpenseID());

        assertTrue(partRepo.getCalledMethods().contains("save"));
        assertEquals(OK, actual.getStatusCode());
    }

    @Test
    public void testAdd() {
        var actual = expenseContr.addExpense(expense);
        assertTrue(expenseRepo.getCalledMethods().contains("save"));
        assertEquals(OK, actual.getStatusCode());
        assertNotNull(actual.getBody());
    }

    @Test
    public void testCantAddEventWithNullAuthor() {
        Expense exp = new Expense();
        exp.setExpenseID(3);

        var actual = expenseContr.addExpense(exp);
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteNothing() {
        Expense exp = new Expense();
        expenseRepo.save(exp);
        var actual = expenseContr.deleteById(24);
        assertEquals(NOT_FOUND, actual.getStatusCode());
    }

    @Test
    public void testDeleteById() {
        Expense exp = new Expense();
        expenseRepo.save(exp);
        var actual = expenseContr.deleteById(exp.getExpenseID());
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void testUpdateExpense() {
        Expense exp = new Expense();
        var updated = expenseContr.updateExpense(26, exp);
        assertEquals(BAD_REQUEST, updated.getStatusCode());
    }
}
