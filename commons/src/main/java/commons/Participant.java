package commons;
import jakarta.persistence.*;

import java.util.*;


@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long participantId;
    private String name;
    private String emailAddress;
    @OneToMany
    private Set<Expense> expenses;

    /**
     * constructor
     */
    public Participant(){}

    /**
     *
     * @param name name of the participant
     * @param email email of the participant. Can be Null
     * @param bankAccount bankAccount number of the participant
     */
    public Participant(String name, String email, String bankAccount) {
        this.name = name;
        this.emailAddress = email;
        new BankAccount(this, bankAccount); //bankAccount should probably be hashed
        expenses = new HashSet<>();
    }

    /**
     * constructor with expenses
     * @param name name of the participant
     * @param email email of the participant. Can be Null
     * @param bankAccount bankAccount number of the participant
     * @param expenses expenses of a participant if it already had some.
     */
    public Participant(String name, String email, String bankAccount, Set<Expense> expenses) {
        this.name = name;
        this.emailAddress = email;
        new BankAccount(this, bankAccount);
        this.expenses = expenses;
    }

    /**
     * getter for ID
     * @return the ID of the participant
     */
    public long getID() {
        return participantId;
    }

    /**
     * setter for id
     * @param id id to be changed to
     */
    public void setID(long id){
        this.participantId = id;
    }

    /**
     * getter for name
     * @return name of the participant
     */
    public String getName() {
        return name;
    }

    /**
     * setter for name
     * @param name name to replace the previous name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get participant
     * @return participant
     */
    public long getParticipantId() {
        return participantId;
    }

    /**
     *
     * @param participantId participantID to replace the old one
     */
    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    /**
     * emailAddress getter. Can be null
     * @return emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     *
     * @param emailAddress that will replace the participants old one
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * getter for expenses
     * @return expenses
     */
    public Set<Expense> getExpenses() {
        return expenses;
    }

    /**
     * setter for expenses
     * @param expenses expenses to replace the old one
     */
    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }

    /**
     *
     * @param o object to be compared to
     * @return true iff all the parameters are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

        if (participantId != that.participantId) return false;
        if (!name.equals(that.name)) return false;
        if (!Objects.equals(emailAddress, that.emailAddress)) return false;
        return expenses.equals(that.expenses);
    }

    /**
     * hashCode generator for class Participant
     * @return a unique hashcode for the object
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(participantId);
        for(Expense e : expenses){
            result += Objects.hash(e.getExpenseID());
        }
        return result;
    }

    /**
     *
     * @return human-readable string of object
     */
    @Override
    public String toString() {
        return "Participant{" +
                "participantId=" + participantId +
                ", name='" + name + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", expenses=" + expenses +
                '}';
    }
}

