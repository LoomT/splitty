package commons;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * Fields:
 *  participantID: ID of the participant to uniquely identify it in the database
 *  name: name of participant
 *  emailAddress: optional email address of participant which can be null
 *  expenseSet: Set of all expenses which the participant authored. (can be empty)
 *  bankAccountSet: The registered Bank Accounts for the participant. (can be empty)
 *  getter setter equals hashcode toString methods
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = Participant.class)
@Entity
@IdClass(EventWeakKey.class)
public class Participant implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Id
    @Column(name = "event_id", length = 5, nullable = false)
    private String eventID;
    @Column(nullable = false)
    private String name;
    @Nullable
    private String emailAddress;
    private String beneficiary;
    private String accountNumber;
    private String bic;

    /**
     * constructor
     */
    public Participant(){}

    /**
     * @param name  name of the participant
     */
    public Participant(String name) {
        this.name = name;
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
     * @param beneficiary bank account beneficiary name
     * @param accountNumber bank account number
     * @param bic bic code
     */
    public Participant(String name, @Nullable String email,
                       String beneficiary, String accountNumber, @Nullable String bic) {
        this(name, email);
        this.beneficiary = beneficiary;
        this.accountNumber = accountNumber;
        this.bic = bic;
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
    public long getId() {
        return id;
    }

    /**
     *
     * @param id participantID to replace the old one
     */
    public void setId(long id) {
        this.id = id;
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
     * @return event id
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Should not be used in the client
     *
     * @param eventID event id
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return bank account beneficiary name
     */
    public String getBeneficiary() {
        return beneficiary;
    }

    /**
     * @param beneficiary bank account beneficiary name
     */
    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    /**
     * @return bank account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * @param accountNumber bank account number
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * @param o object to compare against
     * @return true iff equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return id == that.id && Objects.equals(eventID, that.eventID)
                && Objects.equals(name, that.name)
                && Objects.equals(emailAddress, that.emailAddress)
                && Objects.equals(beneficiary, that.beneficiary)
                && Objects.equals(accountNumber, that.accountNumber)
                && Objects.equals(bic, that.bic);
    }

    /**
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, eventID, name, emailAddress, beneficiary, accountNumber, bic);
    }

    /**
     * toString method for Participant class
     *
     * @return human-readable string
     */
    @Override
    public String toString() {
        String result = "Participant{" +
                "participantId=" + id +
                ", name='" + name + '\'';
        if(emailAddress != null)
            result += ", emailAddress='" + emailAddress + '\'';
        if(beneficiary != null)
            result += ", beneficiary='" + beneficiary + '\'';
        if(accountNumber != null)
            result += "', account number='" + accountNumber + '\'';
        if(bic != null)
            result += ", bic='" + bic + '\'';
        result += "}";
        return result;
    }

    /**
     * Creates and returns a deep copy of this object x such that:
     * <blockquote>
     * <pre>
     * x.clone() != x
     * x.clone().equals(x)</pre></blockquote>
     * and this holds for all non-primitive fields inside recursively
     */
    @Override
    public Participant clone() {
        try{
            return (Participant) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * @return bic
     */
    public String getBic() {
        return bic;
    }

    /**
     * @param bic bic
     */
    public void setBic(String bic) {
        this.bic = bic;
    }
}

