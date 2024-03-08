package commons;
import jakarta.persistence.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Fields:
 *  participantID: ID of the participant to uniquely identify it in the database
 *  name: name of participant
 *  emailAddress: optional email address of participant which can be null
 *  expenseSet: Set of all expenses which the participant authored. (can be empty)
 *  bankAccountSet: The registered Bank Accounts for the participant. (can be empty)
 *  getter setter equals hashcode toString methods
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
     */
    public Participant(String name, @Nullable String email, Set<BankAccount> bankAccountSet ) {
        this(name, email);
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
        return bankAccountSet.equals(that.bankAccountSet);
    }

    /**
     * hashCode generator for class Participant
     * @return a unique hashcode for the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(participantId, name, emailAddress, bankAccountSet);
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
        result += ", bankAccountSet=" + bankAccountSet + '}';
        return result;
    }
}

