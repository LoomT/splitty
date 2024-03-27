package commons;


import jakarta.persistence.*;

import java.text.NumberFormat;
import java.util.*;

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
    private String type;

    /**
     * constructor for Expense class
     * @param expenseAuthor of expense
     * @param purpose of expense
     * @param amount of money
     * @param currency currency, 3 letters
     * @param expenseParticipants participants that split the expense
     * @param type type of expense TODO change to a list of labels when implementing labels
     */
    public Expense(Participant expenseAuthor, String purpose, double amount,
                   String currency, List<Participant> expenseParticipants, String type) {
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
    public String getType() {
        return type;
    }


    /**
     * setter for participant
     * @param expenseAuthor
     */
    public void setExpenseAuthor(Participant expenseAuthor) {
        this.expenseAuthor = expenseAuthor;
    }

    /**
     * setter for purpose
     * @param purpose
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * setter for amount
     * @param amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * setter for currency
     * @param currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * setter for type
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * setter for the ID of the expense
     * @param id
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
     * return form for displaying the expenses in the event page
     * @return human-readable form
     */
    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        NumberFormat currencyFormatter = switch (currency) {
            case "USD" -> NumberFormat.getCurrencyInstance(Locale.US);
            case "EUR" -> NumberFormat.getCurrencyInstance(Locale.GERMANY);
            case "GBP" -> NumberFormat.getCurrencyInstance(Locale.UK);
            case "JPY" -> NumberFormat.getCurrencyInstance(Locale.JAPAN);
            default -> NumberFormat.getCurrencyInstance(Locale.getDefault());
        };

        String formattedAmount = currencyFormatter.format(amount);

        String rez = dayOfMonth + "." + month + "." + year + "     " +
                expenseAuthor.getName() + " paid " +
                formattedAmount + " for " + purpose;

        return rez;
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
