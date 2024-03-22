package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    Participant participant1;
    Participant participant2;
    Expense expense;
    @BeforeEach
    public void testSetup(){
        participant1 = new Participant("participant1", "p1@gmail.com", "Bob",  "1234");
        participant2 = new Participant("participant2", "p2@gmail.com");
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
        assertEquals("participant1", participant1.getName());
    }

    /**
     * tests the setName function
     */
    @Test
    void setNameTest() {
        participant1.setName("participant");
        assertEquals("participant", participant1.getName());
    }

    @Test
    void getEmailAddress() {
        assertEquals("p1@gmail.com", participant1.getEmailAddress());
    }

    @Test
    void setEmailAddress() {
        participant1.setEmailAddress("test@gmail.com");
        assertEquals("test@gmail.com", participant1.getEmailAddress());
    }

    @Test
    void getBeneficiary() {
        assertEquals("Bob", participant1.getBeneficiary());
    }

    @Test
    void getAccountNumber() {
        assertEquals("1234", participant1.getAccountNumber());
    }

    @Test
    void setBeneficiary() {
        participant1.setBeneficiary("Not Bob");
        assertEquals("Not Bob", participant1.getBeneficiary());
    }

    @Test
    void setAccountNumber() {
        participant1.setAccountNumber("Not Bob");
        assertEquals("Not Bob", participant1.getAccountNumber());
    }

    /**
     * tests the true case for the equals function
     */
    @Test
    void testEquals() {
        Participant participant3 = new Participant("participant2", "p2@gmail.com");
        assertEquals(participant2, participant3);
    }

    @Test
    void testEqualsSameReference() {
        Participant participant3 = participant2;
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
        Participant participant3 = new Participant("participant2", "p2@gmail.com");
        assertEquals(participant2.hashCode(), participant3.hashCode());
    }

    @Test
    void testHashCodeOverflow() {
        assertEquals(participant1.hashCode(), participant1.hashCode());
    }
}
