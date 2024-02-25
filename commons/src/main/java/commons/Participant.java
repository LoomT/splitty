package commons;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/*
For now, Participant is just a dummy class used for the variables
inside the Expense class. During the following weeks this will be changed accordingly.
 */

@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long expenseID;

    /**
     * constructor for dummy class Participant
     */
    public Participant() {
    }
}
