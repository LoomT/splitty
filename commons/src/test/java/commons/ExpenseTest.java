package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    Tag t1, t2;
    List<Participant> expPart = new ArrayList<>();
    Date creationDate;
    long expenseId1, expenseId2;
    Expense clone;
    @BeforeEach
    void setup() {
        p1 = new Participant();
        p2 = new Participant();
        expAuth = new Participant();
        expPart.add(p1);
        expPart.add(p2);
        expPart.add(expAuth);
        t1 = new Tag("food", "#00FF00");
        t2 = new Tag("transport", "#0000FF");
        e1 = new Expense(p1, "Groceries", 50.0, "EUR", expPart, t1);
        e2 = new Expense(p1, "Groceries", 50.0, "EUR", expPart, t1);
        e3 = new Expense(p2, "Uber", 20.5, "USD", expPart, t2);

        LocalDate localDate = LocalDate.of(2024, 2, 25);
        creationDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        final AtomicLong sequenceGenerator = new AtomicLong(System.currentTimeMillis());
        expenseId1 = sequenceGenerator.incrementAndGet();
        expenseId2 = sequenceGenerator.incrementAndGet();
        clone = e1.clone();
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
        Expense e = new Expense(p2, "Uber", 20.5, "USD", temp, t2);
        assertTrue(e.getExpenseParticipants().isEmpty());
    }

    @Test
    void testGetExpenseAuthor() {
        assertEquals(p1, e1.getExpenseAuthor());
    }

    @Test
    void testGetPurpose() {
        assertEquals("Groceries", e1.getPurpose());
    }

    @Test
    void testGetAmount() {
        assertEquals("20.50", e3.getAmount().toString());
    }

    @Test
    void testGetCurrency() {
        assertEquals("USD", e3.getCurrency());
    }

    @Test
    void testGetExpenseParticipants() {
        assertEquals(expPart, e2.getExpenseParticipants());
    }

    @Test
    void testGetType() {

        assertEquals(t1, e2.getType());
    }

    @Test
    void testIdUniqueness() {
        assertNotEquals(expenseId1, expenseId2);
    }

    @Test
    void testSetExpenseAuthor() {
        e1.setExpenseAuthor(p2);
        assertEquals(p2, e1.getExpenseAuthor());
    }

    @Test
    void testSetPurpose() {
        e1.setPurpose("Shopping");
        assertEquals("Shopping", e1.getPurpose());
    }

    @Test
    void testSetAmount() {
        e3.setAmount(27.3);
        assertEquals(new BigDecimal("27.3").setScale(2, RoundingMode.HALF_UP), e3.getAmount());
    }

    @Test
    void testSetCurrency() {
        e3.setCurrency("RON");
        assertEquals("RON", e3.getCurrency());
    }

    @Test
    void testSetType() {
        Tag temp = new Tag("Beverages", "FF0000");
        e2.setType(temp);
        assertEquals(temp, e2.getType());
    }

    //Revert changes
    @Test
    void revertChanges() {
        e1.setExpenseAuthor(p1);
        e1.setPurpose("Groceries");
        e3.setAmount(20.5);
        e3.setCurrency("USD");
        e2.setType(t1);
    }

    @Test
    void testEquals() {
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertNotEquals(e2, e3);
    }

    @Test
    void testHashCode() {
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1.hashCode(), e3.hashCode());
        assertNotEquals(e2.hashCode(), e3.hashCode());
    }

    @Test
    void cloneParticipants() {
        assertNotSame(e1.getExpenseParticipants(), clone.getExpenseParticipants());
        assertEquals(e1.getExpenseParticipants(), clone.getExpenseParticipants());
    }

    @Test
    void cloneAuthor() {
        assertNotSame(e1.getExpenseAuthor(), clone.getExpenseAuthor());
        assertEquals(e1.getExpenseAuthor(), clone.getExpenseAuthor());
    }

    @Test
    void clonePurpose() {
        assertEquals(e1.getPurpose(), clone.getPurpose());
    }

    @Test
    void cloneAmount() {
        assertEquals(e1.getAmount(), clone.getAmount());
    }

    @Test
    void cloneCurrency() {
        assertEquals(e1.getCurrency(), clone.getCurrency());
    }

    @Test
    void cloneID() {
        assertEquals(e1.getId(), clone.getId());
    }

    @Test
    void cloneEventID() {
        assertEquals(e1.getEventID(), clone.getEventID());
    }

    @Test
    void cloneDate() {
        assertNotSame(e1.getDate(), clone.getDate());
        assertEquals(e1.getDate(), clone.getDate());
    }

    @Test
    void cloneType() {
        assertEquals(e1.getType(), clone.getType());
    }
}
