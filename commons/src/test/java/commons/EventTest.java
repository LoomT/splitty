package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*

Important to consider, the hash consistency and the eventId
uniqueness cannot be tested without persisting entities yet
should be implemented in the future.


 */


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;


public class EventTest {

    private Event event;
    private List<Participant> list;

    @BeforeEach
    void setUp() {
        list = new ArrayList<>();
        list.add(new Participant());
        event = new Event("Test Event", new ArrayList<>());
    }

    @Test
    void testConstructor() {
        String title = "Test Event";
        List<Participant> participants = new ArrayList<>();
        participants.add(new Participant());
        Event event = new Event(title, participants);
        assertEquals(title, event.getTitle());
        assertEquals(participants.size(), event.getParticipants().size());
        assertTrue(event.getParticipants().containsAll(participants));
        assertNotNull(event.getCreationDate());
    }

    @Test
    void testConstructorNullParticipants() {
        String title = "Test Event";
        Event event = new Event(title, null);
        assertNotNull(event);
        assertEquals(title, event.getTitle());
        assertNotNull(event.getParticipants());
        assertTrue(event.getParticipants().isEmpty());
        assertNotNull(event.getCreationDate());
    }


    @Test
    void testCreationDate() {
        Event event = new Event("Test Event", list);
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
        Event event = new Event("Title", list);
        Event event1;
        event1 = event;
        assertEquals(event1, event);
    }

    @Test
    void testDifferentInstance() {
        Event event1 = new Event("Title", list);
        Event event2 = new Event("Title", list);
        assertEquals(event1, event2);
    }

    @Test
    void testEqualityWithoutId(){
        Event event1 = new Event("Title", list);
        Event event2 = new Event("Title", list);
        assertEquals(event1.getTitle(), event2.getTitle());
        assertEquals(event1.getParticipants().size(), event2.getParticipants().size());
        for(int i = 0; i < event1.getParticipants().size(); i++){
            assertEquals(event1.getParticipants().get(i), event2.getParticipants().get(i));
        }
    }


    @Test
    void testNull() {
        Event event = new Event("Title", list);
        assertNotEquals(null, event);
    }

    @Test
    void testEqualityDifferentClass() {
        Event event = new Event("Title", list);
        Object other = new Object();
        assertNotEquals(event, other);
    }

    @Test
    void testHashConsistency() {
        Event event1 = new Event("Title", list);
        Event event2 = new Event("Title", list);
        assertEquals(event1.hashCode(), event2.hashCode()); // should be equal due to unique ID not
        // taken into consideration
        //can later be tested differently when object persistence is implemented with a database
    }

    @Test
    void addingParticipantTest() {
        Participant participant = new Participant();
        event.addParticipant(participant);
        assertEquals(event.getParticipants().get(0), participant);
    }

    @Test
    void settingParticipantTest(){
        List<Participant> testList = new ArrayList<>();
        testList.add(new Participant());
        testList.add(new Participant());
        testList.add(new Participant());
        testList.add(new Participant());
        Event event1 = new Event("title", null);
        event1.setParticipants(testList);
        Event event2 = new Event("title", testList);
        assertEquals(event1.getParticipants(), event2.getParticipants());
    }

    @Test
    void testDeleteParticipant() {
        Participant participant = new Participant();
        event.addParticipant(participant);
        assertTrue(event.deleteParticipant(participant));
        assertFalse(event.getParticipants().contains(participant));
    }

    @Test
    void testDeleteNothing() {
        Participant participant = new Participant();
        event.deleteParticipant(participant);
        assertFalse(event.getParticipants().contains(participant));
    }

    @Test
    void testParticipantList() {
        Participant participant1 = new Participant();
        Participant participant2 = new Participant();
        event.addParticipant(participant1);
        event.addParticipant(participant2);
        event.deleteParticipant(participant1);
        assertTrue(event.getParticipants().contains(participant2) && event.getParticipants().size() == 1);
    }
}