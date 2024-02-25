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
     * tests the getExpenseList function
     */
    @Test
    void getExpenseListTest() {
        assertEquals(participant1.getAttributedExpenseList(),new ArrayList<>());
    }

    /**
     * tests the setExpenseList function
     */
    @Test
    void setExpenseListTest() {
        testSet.add(expense);
        participant1.setAttributedExpenseList(testSet);
        assertEquals(participant1.getAttributedExpenseList(),testSet);
    }

    /**
     * tests the setExpense function when an Expense can be added.
     * The function returns true if Expense isn't null and the Expense itself
     * isn't in the ExpenseList.
     */
    @Test
    void addExpenseWhenTrue() {
        Expense testExpense = new Expense(participant2, "test2", 45,
                "EUR", new ArrayList<>(), "type"); // different from expense
        assertTrue(participant1.addExpense(testExpense));
    }

    /**
     * tests the setExpense function when an Expense can be added.
     * The function returns false if Expense is null or Expense itself
     * is in the ExpenseList.
     */
    @Test
    void addExpenseWhenFalse() {
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

    @Test
    void testHashRecursion() {
        participant1.addExpense(expense);
        expense.hashCode();
    }
}
