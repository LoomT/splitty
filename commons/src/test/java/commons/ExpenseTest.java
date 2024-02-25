package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/*
Remark:
 */

public class ExpenseTest {
    Expense e1, e2, e3;
    Participant p1, p2, expAuth;
    List<Participant> expPart = new ArrayList<>();
    Date creationDate;
    long expenseId1, expenseId2;
    @BeforeEach
    void setup() {
        p1 = new Participant();
        p2 = new Participant();
        expAuth = new Participant();
        expPart.add(p1);
        expPart.add(p2);
        expPart.add(expAuth);

        e1 = new Expense(p1, "Groceries", 50.0, "EUR", expPart, "Food");
        e2 = new Expense(p1, "Groceries", 50.0, "EUR", expPart, "Food");
        e3 = new Expense(p2, "Uber", 20.5, "USD", expPart, "Transport");

        LocalDate localDate = LocalDate.of(2024, 2, 25);
        creationDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        final AtomicLong sequenceGenerator = new AtomicLong(System.currentTimeMillis());
        expenseId1 = sequenceGenerator.incrementAndGet();
        expenseId2 = sequenceGenerator.incrementAndGet();

    }

    @Test
    void testConstructor() {
        assertNotNull(e1);
        assertNotNull(e2);
        assertNotNull(e3);
    }

    @Test
    void testConstructorNoParticipants() {
        List<Participant> temp = new ArrayList<>();
        Expense e = new Expense(p2, "Uber", 20.5, "USD", temp, "Transport");
        assertTrue(e.getExpenseParticipants().isEmpty());
    }

    @Test
    void testGetters() {
        assertEquals(p1, e1.getExpenseAuthor());
        assertEquals("Groceries", e1.getPurpose());
        assertEquals(20.5, e3.getAmount());
        assertEquals("USD", e3.getCurrency());
        assertEquals(expPart, e2.getExpenseParticipants());
        assertEquals("Food", e2.getType());
    }

    @Test
    void testIdUniqueness() {
        assertNotEquals(expenseId1, expenseId2);
    }


}
