package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    Participant participant1;
    Participant participant2;
    Expense expense;
    List<Expense> testSet;
    @BeforeEach
    public void testSetup(){
        participant1 = new Participant("participant1");
        participant2 = new Participant("participant2");
        testSet = new ArrayList<>();
        List<Participant> expenseParticipants = new ArrayList<>();
        expenseParticipants.add(participant1);
        expenseParticipants.add(participant2);
        expense = new Expense(participant1, "test", 32,
                "EUR", expenseParticipants, "type");

    }


    /**
     * tests the getName function
     */
    @Test
    void getNameTest() {
        assertEquals(participant1.getName(), "participant1");
    }

    /**
     * tests the setName function
     */
    @Test
    void setNameTest() {
        participant1.setName("participant");
        assertEquals(participant1.getName(), "participant");
    }

    /**
     * tests the true case for the equals function
     */
    @Test
    void testEquals() {
        Participant participant3 = new Participant("participant2");
        assertEquals(participant2, participant3);
    }

    /**
     * tests the false case for the equals function
     */
    @Test
    void testNotEquals() {
        assertNotEquals(participant1, participant2);
    }

    /**
     * tests the hashcode function
     */
    @Test
    void testHashCode() {
        Participant participant3 = new Participant("participant2");
        assertEquals(participant2.hashCode(), participant3.hashCode());
    }
}
