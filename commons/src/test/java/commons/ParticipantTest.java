package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    Participant participant1;
    Participant participant2;
    Expense expense;
    BankAccount bankAccount;
    @BeforeEach
    public void testSetup(){
        participant1 = new Participant("participant1", "p1@gmail.com");
        participant2 = new Participant("participant2", "p2@gmail.com");
        List<Participant> expenseParticipants = new ArrayList<>();
        bankAccount = new BankAccount("test","1234");
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

    @Test
    void getEmailAddress() {
        assertEquals(participant1.getEmailAddress(), "p1@gmail.com");
    }

    @Test
    void setEmailAddress() {
        participant1.setEmailAddress("test@gmail.com");
        assertEquals(participant1.getEmailAddress(), "test@gmail.com");
    }

    @Test
    void getBankAccountSet() {
        assertEquals(participant1.getBankAccountSet(), new HashSet<>());
    }

    @Test
    void setBankAccountSet() {
        assertEquals(participant1.getBankAccountSet(), new HashSet<>());
        Set<BankAccount> test = new HashSet<>();
        test.add(bankAccount);
        participant1.setBankAccountSet(test);
        assertEquals(participant1.getBankAccountSet(), test);
    }

    @Test
    void addBankAccountTrue() {
        assertTrue(participant1.addBankAccount(bankAccount));
    }

    @Test
    void addBankAccountFalse() {
        assertTrue(participant1.addBankAccount(bankAccount));
        assertFalse(participant1.addBankAccount(bankAccount));
    }

    @Test
    void addBankAccountNull() {
        assertFalse(participant1.addBankAccount(null));
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
