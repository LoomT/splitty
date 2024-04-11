package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    Participant giver;
    Participant receiver;
    Transaction t;
    @BeforeEach
    void setUp() {
        giver = new Participant("Tom");
        receiver = new Participant("Bob");
        t = new Transaction(giver, receiver, 50, "EUR");
    }

    @Test
    void setId() {
        t.setId(3);
        assertEquals(3, t.getId());
    }

    @Test
    void setEventID() {
        t.setEventID("ABCDE");
        assertEquals("ABCDE", t.getEventID());
    }

    @Test
    void getGiver() {
        assertEquals(giver, t.getGiver());
    }

    @Test
    void getReceiver() {
        assertEquals(receiver, t.getReceiver());
    }

    @Test
    void setGiver() {
        Participant p = new Participant("Kyle");
        t.setGiver(p);
        assertEquals(p, t.getGiver());
    }

    @Test
    void setReceiver() {
        Participant p = new Participant("Kyle");
        t.setReceiver(p);
        assertEquals(p, t.getReceiver());
    }

    @Test
    void getAmount() {
        assertEquals(50, t.getAmount());
    }

    @Test
    void testEquals() {
        Transaction t2 = new Transaction(giver, receiver, 50, "EUR", t.getDate());
        assertEquals(t, t2);
    }

    @Test
    void testEqualsDifferentAmount() {
        Transaction t2 = new Transaction(giver, receiver, 70, "EUR");
        assertNotEquals(t, t2);
    }

    @Test
    void testHashCode() {
        Transaction t2 = new Transaction(giver, receiver, 50, "EUR", t.getDate());
        assertEquals(t.hashCode(), t2.hashCode());
    }
    @Test
    void testDifferentHashCode() {
        Transaction t2 = new Transaction(giver, receiver, 70, "EUR");
        assertNotEquals(t.hashCode(), t2.hashCode());
    }

    @Test
    void testToStringGiver() {
        assertTrue(t.toString().contains("Tom"));
    }
    @Test
    void testToStringReceiver() {
        assertTrue(t.toString().contains("Bob"));
    }
    @Test
    void testToStringAmount() {
        assertTrue(t.toString().contains("50"));
    }
    @Test
    void testToStringEventID() {
        t.setEventID("ABCDE");
        assertTrue(t.toString().contains("ABCDE"));
    }
    @Test
    void testToStringCurrency() {
        assertTrue(t.toString().contains("EUR"));
    }

    @Test
    void cloneIDs() {
        Transaction clone = t.clone();
        assertEquals(t.getId(), clone.getId());
        assertEquals(t.getEventID(), clone.getEventID());
    }

    @Test
    void cloneAmount() {
        Transaction clone = t.clone();
        assertEquals(t.getAmount(), clone.getAmount());
    }

    @Test
    void cloneParticipants() {
        Transaction clone = t.clone();
        assertNotSame(t.getGiver(), clone.getGiver());
        assertEquals(t.getGiver(), clone.getGiver());
        assertNotSame(t.getReceiver(), clone.getReceiver());
        assertEquals(t.getReceiver(), clone.getReceiver());
    }

    @Test
    void getCurrency() {
        assertEquals("EUR", t.getCurrency());
    }

    @Test
    void compareTo() {
        Transaction t2 = new Transaction(giver, receiver, 70, "EUR", Date.from(Instant.EPOCH));
        Transaction t3 = new Transaction(giver, receiver, 70, "EUR", new GregorianCalendar(2050, Calendar.APRIL, 12).getTime());
        List<Transaction> sorted = Stream.of(t, t2, t3).sorted().toList();
        assertEquals(t3, sorted.getFirst());
        assertEquals(t2, sorted.getLast());
    }
}