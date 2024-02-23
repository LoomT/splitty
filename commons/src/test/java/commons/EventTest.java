package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;




import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;


public class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event("Test Event", new ArrayList<>());
    }

    @Test
    void testConstructor() {
        String title = "Test Event";
        List<String> participants = Arrays.asList("Person1", "Person2");
        Event event = new Event(title, participants);
        assertNotNull(event.getEventID());
        assertEquals(title, event.getTitle());
        assertEquals(participants.size(), event.getParticipants().size());
        assertTrue(event.getParticipants().containsAll(participants));
        assertNotNull(event.getCreationDate());
    }

    @Test
    void testConstructorNullParticipants() {
        String title = "Test Event";
        Event event = new Event(title, null);
        assertNotNull(event.getEventID());
        assertEquals(title, event.getTitle());
        assertNull(event.getParticipants());
        assertNotNull(event.getCreationDate());
    }

    @Test
    void testEventIDUnique() {
        Event event1 = new Event("Event 1", List.of("Participant 1"));
        Event event2 = new Event("Event 2", List.of("Participant 2"));
        assertNotEquals(event1.getEventID(), event2.getEventID());
    }

    @Test
    void testCreationDate() {
        Event event = new Event("Test Event", List.of("John Doe"));
        assertNotNull(event.getCreationDate());
        assertTrue(event.getCreationDate().getTime() <= System.currentTimeMillis());
    }

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
        assertNotEquals(event1, event2);
    }

    @Test
    void testNotEquals() {
        Event event1 = new Event("Title", List.of("Participant1"));
        Event event2 = new Event("Title", List.of("Participant1"));
        assertNotEquals(event1, event2); // not equal due to unique id
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
        assertNotEquals(event1.hashCode(), event2.hashCode()); //not equal due to unique ID
    }

    @Test
    void addingParticipantTest() {
        String participant = "Person123";
        event.addParticipant(participant);
        assertTrue(event.getParticipants().contains(participant));
    }

    @Test
    void testDeleteParticipant() {
        String participant = "Person123";
        event.addParticipant(participant);
        event.deleteParticipant(participant);
        assertFalse(event.getParticipants().contains(participant));
    }

    @Test
    void testDeleteNothing() {
        String participant = "Person123";
        event.deleteParticipant(participant);
        assertFalse(event.getParticipants().contains(participant));
    }

    @Test
    void testParticipantList() {
        String participant1 = "Person123";
        String participant2 = "Person123";
        event.addParticipant(participant1);
        event.addParticipant(participant2);
        event.deleteParticipant(participant1);
        assertTrue(event.getParticipants().contains(participant2) && event.getParticipants().size() == 1);
    }
}
