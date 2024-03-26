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
     */
    public Participant(String name, @Nullable String email,
                       String beneficiary, String accountNumber ) {
        this(name, email);
        this.beneficiary = beneficiary;
        this.accountNumber = accountNumber;
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
                && Objects.equals(accountNumber, that.accountNumber);
    }

    /**
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, eventID, name, emailAddress, beneficiary, accountNumber);
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
        if(beneficiary != null && accountNumber != null)
            result += ", beneficiary='" + beneficiary
                    + "', account number='" + accountNumber + '\'';
        result += "}";
        return result;
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     * <p>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @implSpec The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p>
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     * @see Cloneable
     */
    @Override
    public Participant clone() {
        try{
            return (Participant) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

