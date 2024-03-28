package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//TODO implement DI and test id generation
public class EventTest {

    private Event event;
    private Event event1;
    private Event event2;
    List<Participant> participantList;
    List<Expense> expenseList;

    @BeforeEach
    void setUp() {
        participantList = new ArrayList<>();
        participantList.add(new Participant());
        expenseList = new ArrayList<>();
        expenseList.add(new Expense());
        event = new Event("Test Event", new ArrayList<>(), null);
        event1 = new Event("Title", List.of(new Participant("Person1", "p1"),
                new Participant("Person2", "p2")), null);
        event2 = new Event("Title", List.of(new Participant("Person1", "p1"),
                new Participant("Person2", "p2")), null);
    }

    @Test
    void testConstructor() {
        String title = "Test Event";
        List<Participant> participants = Arrays.asList(new Participant("Person1", "p1"),
                new Participant("Person2", "p2"));
        Event event = new Event(title, participants, null);
        assertEquals(title, event.getTitle());
        assertEquals(participants.size(), event.getParticipants().size());
        assertTrue(event.getParticipants().containsAll(participants));
        assertNotNull(event.getCreationDate());
    }

    @Test
    void testConstructorNullParticipants() {
        String title = "Test Event";
        Event event = new Event(title, null, null);
        assertNotNull(event);
        assertEquals(title, event.getTitle());
        assertNotNull(event.getParticipants());
        assertTrue(event.getParticipants().isEmpty());
        assertNotNull(event.getCreationDate());
    }


    @Test
    void testCreationDate() {
        Event event = new Event("Test Event", List.of(
                new Participant("John Doe", "jd")), null);
        assertNotNull(event.getCreationDate());
        assertTrue(event.getCreationDate().getTime() <= System.currentTimeMillis());
    }

    @Test
    public void EventGetterTest(){
        String title = "title";
        Event test = new Event(title, null, null);
        assertEquals("title", test.getTitle());
    }

    @Test
    public void EventSetterTest(){
        String title = "title";
        Event test = new Event(title, null, null);
        assertEquals("title", test.getTitle());
        test.setTitle("newTitle");
        assertEquals("newTitle", test.getTitle());
    }

    @Test
    void testSameInstance() {
        Event testEvent = event;
        assertEquals(testEvent, event);
    }

    @Test
    void testDifferentInstance() {
        Event event1 = new Event("Title", participantList, null);
        Event event2 = new Event("Title", participantList, null);
        assertEquals(event1, event2);
    }

    @Test
    void testEqualityWithoutId(){
        assertEquals(event1.getTitle(), event2.getTitle());
        assertEquals(event1.getParticipants().size(), event2.getParticipants().size());
        for(int i = 0; i < event1.getParticipants().size(); i++){
            assertEquals(event1.getParticipants().get(i), event2.getParticipants().get(i));
        }
    }
    @Test
    void testNull() {
        assertNotEquals(null, event);
    }

    @Test
    void testEqualityDifferentClass() {
        Object other = new Object();
        assertNotEquals(event, other);
    }

    @Test
    void testHashConsistency() {
        assertEquals(event1.hashCode(), event2.hashCode());
        // should be equal due to unique ID not taken into consideration
        //can later be tested differently when object persistence is implemented with a database
    }

    @Test
    void addingParticipantTest() {
        Participant participant = new Participant("Person123", "test123");
        event.addParticipant(participant);
        assertEquals(event.getParticipants().getFirst(), participant);
    }

    @Test
    void settingParticipantTest(){
        List<Participant> testList = new ArrayList<>();
        for(int i = 0; i<4; i++){
            testList.add(new Participant("Person"+i, "test"+i));
        }
        Event event1 = new Event("title", null, null);
        event1.setParticipants(testList);
        Event event2 = new Event("title", testList, null);
        assertEquals(event1.getParticipants(), event2.getParticipants());
    }

    @Test
    void testDeleteParticipant() {
        Participant participant = new Participant("Person", "test");
        event.addParticipant(participant);
        assertTrue(event.deleteParticipant(participant));
        assertFalse(event.getParticipants().contains(participant));
    }

    @Test
    void addingExpenseTest() {
        Participant author = new Participant("test", "test");
        Expense expense  = new Expense(author, "test", 2.0, "EUR",
                new ArrayList<>(), "test");
        event.addExpense(expense);
        assertEquals(event.getExpenses().getFirst(), expense);
    }

    @Test
    void settingExpenseTest(){
        List<Expense> testList = new ArrayList<>();
        Participant author = new Participant("test", "test");
        for(int i = 0; i<4; i++){
            testList.add(new Expense(author, "test" + i, 2.0, "EUR",
                    new ArrayList<>(), "test" + i));
        }
        Event event1 = new Event("title", null, null);
        event1.setExpenses(testList);
        Event event2 = new Event("title", null, testList);
        assertEquals(event1.getExpenses(), event2.getExpenses());
    }

    @Test
    void testExpenseParticipant() {
        Participant author = new Participant("test", "test");
        Expense expense  = new Expense(author, "test", 2.0, "EUR",
                new ArrayList<>(), "test");
        event.addExpense(expense);
        assertTrue(event.deleteExpense(expense));
        assertFalse(event.getExpenses().contains(expense));
    }

    @Test
    void testDeleteNothing() {
        Participant author = new Participant("test", "test");
        Expense expense  = new Expense(author, "test", 2.0, "EUR",
                new ArrayList<>(), "test");
        assertFalse(event.deleteExpense(expense));
        assertFalse(event.getExpenses().contains(expense));
    }

    @Test
    void testParticipantList() {
        Participant author = new Participant("test", "test");
        Expense expense1  = new Expense(author, "test", 2.0, "EUR",
                new ArrayList<>(), "test");
        Expense expense2  = new Expense(author, "test", 2.0, "EUR",
                new ArrayList<>(), "test");
        event.addExpense(expense1);
        event.addExpense(expense2);
        event.deleteExpense(expense1);
        assertTrue(event.getExpenses().contains(expense2) && event.getExpenses().size() == 1);
    }

    @Test
    void lastActivityConstructed() {
        assertNotNull(event.getLastActivity());
    }

    @Test
    void setActivity() {
        Date date = new Date();
        event.setLastActivity(date);
        assertEquals(date, event.getLastActivity());
    }

    @Test
    void cloneCreationDate() {
        Event event = new Event("title");
        Event clone = event.clone();
        assertNotSame(event.getCreationDate(), clone.getCreationDate());
        assertEquals(event.getCreationDate(), clone.getCreationDate());
    }

    @Test
    void cloneLastActivity() {
        Event event = new Event("title");
        Event clone = event.clone();
        assertNotSame(event.getLastActivity(), clone.getLastActivity());
        assertEquals(event.getLastActivity(), clone.getLastActivity());
    }

    @Test
    void cloneTitle() {
        Event event = new Event("title");
        Event clone = event.clone();
        assertEquals(event.getTitle(), clone.getTitle());
    }

    @Test
    void cloneID() {
        Event event = new Event("title");
        Event clone = event.clone();
        assertEquals(event.getId(), clone.getId());
    }

    @Test
    void cloneParticipants() {
        Event event = new Event("title");
        Event clone = event.clone();
        assertNotSame(event.getParticipants(), clone.getParticipants());
        assertEquals(event.getParticipants(), clone.getParticipants());
    }

    @Test
    void cloneExpenses() {
        Event event = new Event("title");
        Event clone = event.clone();
        assertNotSame(event.getExpenses(), clone.getExpenses());
        assertEquals(event.getExpenses(), clone.getExpenses());
    }
}
