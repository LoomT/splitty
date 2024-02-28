package commons;
import jakarta.persistence.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Class for testing purposes
 */
@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long participantId;
    private String name;
    @Nullable
    private String emailAddress;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Expense> expenseSet;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<BankAccount> bankAccountSet;

    /**
     * constructor
     */
    public Participant(){}

    /**
     * @param name  name of the participant
     */
    public Participant(String name) {
        this.name = name;
        emailAddress = null;
        expenseSet = new HashSet<>();
        bankAccountSet = new HashSet<>();
    }

    /**
     * @param name  name of the participant
     * @param email email of the participant. Can be Null
     */
    public Participant(String name, @Nullable String email) {
        this(name);
        this.emailAddress = email;
    }

    /**
     * constructor with expenses
     * @param name name of the participant
     * @param email email of the participant. Can be Null
     * @param bankAccountSet bankAccount number of the participant
     * @param expenseSet expenses of a participant if it already had some.
     */
    public Participant(String name, @Nullable String email, Set<Expense> expenseSet,
                       Set<BankAccount> bankAccountSet ) {
        this(name, email);
        this.expenseSet = expenseSet;
        this.bankAccountSet = bankAccountSet;
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
    public @Nullable String getEmailAddress() {
        return emailAddress;
    }

    /**
     *
     * @param emailAddress that will replace the participants old one
     */
    public void setEmailAddress(@Nullable String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * getter for expenses
     * @return expenses
     */
    public Set<Expense> getExpenseSet() {
        return expenseSet;
    }

    /**
     * setter for expenses
     * @param expenseSet expenses to replace the old one
     */
    public void setExpenseSet(Set<Expense> expenseSet) {
        this.expenseSet = expenseSet;
    }

    /**
     * Add bankAccount to bankAccountSet
     * @param expense expense to be added
     * @return false if bankAccount is null or already in set, true otherwise
     */
    public boolean addExpense(Expense expense){
        if(expense == null) return false;
        return expenseSet.add(expense);
    }

    /**
     * getter for bankAccountSet
     * @return bankAccountSet
     */
    public Set<BankAccount> getBankAccountSet() {
        return bankAccountSet;
    }

    /**
     * setter for bankAccountSet
     * @param bankAccountSet to replace the old one
     */
    public void setBankAccountSet(Set<BankAccount> bankAccountSet) {
        this.bankAccountSet = bankAccountSet;
    }

    /**
     * Add bankAccount to bankAccountSet
     * @param bankAccount bankAccount to be added
     * @return false if bankAccount is null or already in set, true otherwise
     */
    public boolean addBankAccount(BankAccount bankAccount){
        if(bankAccount == null) return false;
        return bankAccountSet.add(bankAccount);
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

        if (participantId != that.participantId || !name.equals(that.name)) return false;
        if (!Objects.equals(emailAddress, that.emailAddress)) return false;
        if (!expenseSet.equals(that.expenseSet)) return false;
        return bankAccountSet.equals(that.bankAccountSet);
    }

    /**
     * hashCode generator for class Participant
     * @return a unique hashcode for the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(participantId);
    }

    /**
     * toString method for Participant class
     * @return human-readable string
     */
    @Override
    public String toString() {
        String result = "Participant{" +
                "participantId=" + participantId +
                ", name='" + name + '\'';
        if(emailAddress != null)
            result += ", emailAddress='" + emailAddress + '\'';
        result += ", expenseSet=" + expenseSet +
                ", bankAccountSet=" + bankAccountSet +
                '}';
        return result;
    }
}

