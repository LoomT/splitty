package commons;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@IdClass(EventWeakKey.class)
public class Expense implements Cloneable {
    /*
    Properties:
    Int expenseID so that one can reuse this type of expense
    String participant for the person that paid the expense
    String purpose for the purpose of the existent expense
    Double amount for the amount paid by the participant
    String currency for the currency that was used then
    Date date for the exact date when the expense was paid
    Boolean splitMethod, 0 - equally among all participant, 1- only a part of them
    List<String> participants for all the people that are splitting the expense
    String type for the type of the current created expense
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Id
    @Column(name = "event_id", length = 5, nullable = false)
    private String eventID;
    @ManyToOne
    private Participant expenseAuthor;
    @Column(nullable = false)
    private String purpose;
    @Column(nullable = false)
    private double amount;
    @Column(length = 3, nullable = false)
    private String currency;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Participant> expenseParticipants;
    @ManyToOne
    @JoinColumns(value = {
        @JoinColumn(name = "type_id", referencedColumnName = "id"),
        @JoinColumn(name = "type_event_id", referencedColumnName = "event_id")
    },
        foreignKey = @ForeignKey(name = "expense_tag",
            foreignKeyDefinition = "FOREIGN KEY (type_id, type_event_id)" +
            "REFERENCES tag(id, event_id) ON UPDATE CASCADE ON DELETE SET NULL"))
    private Tag type;

    /**
     * constructor for Expense class
     * @param expenseAuthor of expense
     * @param purpose of expense
     * @param amount of money
     * @param currency currency, 3 letters
     * @param expenseParticipants participants that split the expense
     * @param type type of expense
     */
    public Expense(Participant expenseAuthor, String purpose, double amount,
                   String currency, List<Participant> expenseParticipants, Tag type) {
        this.expenseAuthor = expenseAuthor;
        this.purpose = purpose;
        this.amount = amount;
        this.currency = currency;
        this.date = new Date();
        this.expenseParticipants = expenseParticipants;
        this.type = type;
    }

    /**
     * no arg constructor for the Expense class
     */
    public Expense() {

    }


    /**
     * getter for expenseID
     * @return the expenseID
     */
    public long getId() {
        return id;
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
     * getter for participant
     * @return the participant
     */
    public Participant getExpenseAuthor() {
        return expenseAuthor;
    }

    /**
     * getter for purpose
     * @return the purpose
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * getter for amount
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * getter for currency
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * getter for date
     * @return the date when the expense was created
     */
    public Date getDate() {
        return date;
    }

    /**
     * getter for the list of participants
     * @return the list of participants
     */
    public List<Participant> getExpenseParticipants() {
        return expenseParticipants;
    }

    /**
     * @param newParticipants expense participant list to set
     */
    public void setExpenseParticipants(List<Participant> newParticipants) {
        expenseParticipants = newParticipants;
    }

    /**
     * getter for type
     * @return the type of expense
     */
    public Tag getType() {
        return type;
    }


    /**
     * setter for participant
     * @param expenseAuthor participant that made the expense
     */
    public void setExpenseAuthor(Participant expenseAuthor) {
        this.expenseAuthor = expenseAuthor;
    }

    /**
     * setter for purpose
     * @param purpose purpose of the expense
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * setter for amount
     * @param amount cost of the expense in specified currency
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * setter for currency
     * @param currency currency of the cost
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * setter for type
     * @param type type of expense
     */
    public void setType(Tag type) {
        this.type = type;
    }

    /**
     * setter for the ID of the expense
     * @param id id of expense
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * setter for date
     * @param date the updated value of the date
     */
    public void setDate(Date date) {
        this.date = date;
    }


    /**
     * @param o object to compare against
     * @return true iff equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id == expense.id
                && Double.compare(amount, expense.amount) == 0
                && Objects.equals(eventID, expense.eventID)
                && Objects.equals(expenseAuthor, expense.expenseAuthor)
                && Objects.equals(purpose, expense.purpose)
                && Objects.equals(currency, expense.currency)
                && Objects.equals(date, expense.date)
                && Objects.equals(expenseParticipants, expense.expenseParticipants)
                && Objects.equals(type, expense.type);
    }

    /**
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, eventID, expenseAuthor, purpose,
                amount, currency, date, expenseParticipants, type);
    }


    /**
     * @return the string representation of this object
     */
    @Override
    public String toString() {
        StringBuilder participantIds = new StringBuilder();
        for (Participant p : expenseParticipants) {
            participantIds.append(p.getName()).append(", ");
        }
        return "Expense{" +
                "id=" + id +
                ", eventID='" + eventID + '\'' +
                ", expenseAuthor=" + expenseAuthor.getId() +
                ", purpose='" + purpose + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", date=" + date +
                ", expenseParticipants=" + participantIds +
                ", type='" + type + '\'' +
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
    public Expense clone() {
        try {
            Expense clone = (Expense) super.clone();
            clone.expenseAuthor = this.expenseAuthor.clone();
            clone.expenseParticipants = new ArrayList<>();
            for (Participant p : this.expenseParticipants) {
                clone.expenseParticipants.add(p.clone());
            }
            clone.date = (Date) this.date.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
