package commons;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@IdClass(EventWeakKey.class)
public class Transaction implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Id
    @Column(name = "event_id", length = 5, nullable = false)
    private String eventID;
    @OneToOne
    private Participant giver;
    @OneToOne
    private Participant receiver;
    private double amount;

    /**
     * Constructor for JPA
     */
    public Transaction() {
    }

    /**
     * @param giver participant that paid
     * @param receiver participant that received
     * @param amount amount paid in default currency
     */
    public Transaction(Participant giver, Participant receiver, double amount) {
        this.giver = giver;
        this.receiver = receiver;
        this.amount = amount;
    }

    /**
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return event id
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * @param eventID event id
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return participant that paid in this transaction
     */
    public Participant getGiver() {
        return giver;
    }

    /**
     * @param giver participant that paid in this transaction
     */
    public void setGiver(Participant giver) {
        this.giver = giver;
    }

    /**
     * @return participant that received in this transaction
     */
    public Participant getReceiver() {
        return receiver;
    }

    /**
     * @param receiver participant that received in this transaction
     */
    public void setReceiver(Participant receiver) {
        this.receiver = receiver;
    }

    /**
     * @return amount in default currency
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param o object to compare against
     * @return true iff equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id && Double.compare(amount, that.amount) == 0
                && Objects.equals(eventID, that.eventID)
                && Objects.equals(giver, that.giver)
                && Objects.equals(receiver, that.receiver);
    }

    /**
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, eventID, giver, receiver, amount);
    }

    /**
     * @return readable string of this object
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", eventID='" + eventID + '\'' +
                ", giver=" + giver +
                ", receiver=" + receiver +
                ", amount=" + amount +
                '}';
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
    public Transaction clone() {
        try {
            Transaction clone = (Transaction) super.clone();
            clone.giver = this.giver.clone();
            clone.receiver = this.receiver.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Clones the transaction and links the participant with
     * participants of given event
     *
     * @param event event
     * @return transaction
     */
    public Transaction clone(Event event) {
        try {
            Transaction clone = (Transaction) super.clone();
            clone.receiver = event.getParticipants().stream()
                    .filter(p -> p.getName().equals(receiver.getName())).findFirst().orElseThrow();
            clone.giver = event.getParticipants().stream()
                    .filter(p -> p.getName().equals(giver.getName())).findFirst().orElseThrow();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
