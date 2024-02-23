package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.util.*;


public class EventTest {

    @Test
    public void EventGetterTest(){
        String title = "title";
        Event test = new Event(title, null);
        assertEquals("title", test.getTitle());
    }

    @Test
    public void EventSetterTest(){
        String title = "title";
        Event test = new Event(title, null);
        assertEquals("title", test.getTitle());
        test.setTitle("newTitle");
        assertEquals("newTitle", test.getTitle());
    }

    @Test
    void testSameInstance() {
        Event event = new Event("Title", List.of("Participant1", "Participant2"));
        assertEquals(event, event);
    }

    @Test
    void testDifferentInstance() {
        Event event1 = new Event("Title", List.of("Participant1", "Participant2"));
        Event event2 = new Event("Title", List.of("Participant1", "Participant2"));
        assertEquals(event1, event2);
    }

    @Test
    void testNotEquals() {
        // This test assumes eventIDs are set differently which should be the case by design
        Event event1 = new Event("Title", List.of("Participant1"));
        Event event2 = new Event("Title", List.of("Participant1"));
        assertNotEquals(event1, event2);
    }

    @Test
    void testNull() {
        Event event = new Event("Title", List.of("Participant1"));
        assertNotEquals(null, event);
    }

    @Test
    void testEqualityDifferentClass() {
        Event event = new Event("Title", List.of("Participant1"));
        Object other = new Object();
        assertNotEquals(event, other);
    }

    @Test
    void testHashConsistency() {
        Event event1 = new Event("Title", List.of("Participant1"));
        Event event2 = new Event("Title", List.of("Participant1"));
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
