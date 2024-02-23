package commons;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    Participant participant1 = new Participant("participant1");
    Participant participant2 = new Participant("participant2");
    Set<Expense> testSet = new HashSet<>();
    Expense expense = new Expense(2, 32, "created for tests",
            "test", participant1, participant2);

    /**
     * tests the getName function
     */
    @Test
    void getName() {
        assertEquals(participant1.getName(), "participant1");
    }

    /**
     * tests the setName function
     */
    @Test
    void setName() {
        participant1.setName("participant");
        assertEquals(participant1.getName(), "participant");
    }

    /**
     * tests the getExpenseList function
     */
    @Test
    void getExpenseList() {
        assertEquals(participant1.getExpenseList(),new HashSet<>());
    }

    /**
     * tests the setExpenseList function
     */
    @Test
    void setExpenseList() {
        testSet.add(expense);
        participant1.setExpenseList(testSet);
        assertEquals(participant1.getExpenseList(),testSet);
    }

    /**
     * tests the setExpense function when an Expense can be added.
     * The function returns true if Expense isn't null and the Expense itself
     * isn't in the ExpenseList.
     */
    @Test
    void addExpenseTrue() {
        Expense testExpense = new Expense(3, 15, "test2",
                "testExpense2", participant1, participant2);
        assertTrue(participant1.addExpense(testExpense));
    }

    /**
     * tests the setExpense function when an Expense can be added.
     * The function returns false if Expense is null or Expense itself
     * is in the ExpenseList.
     */
    @Test
    void addExpenseFalse() {
        participant1.addExpense(expense);
        assertFalse(participant1.addExpense(expense));
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
