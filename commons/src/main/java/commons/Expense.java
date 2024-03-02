package commons;


import jakarta.persistence.*;
import org.jetbrains.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class Expense {
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long expenseID;
    @NotNull
    @ManyToOne
    private Participant expenseAuthor;
    @NotNull
    private String purpose;
    @NotNull
    private double amount;
    @NotNull
    private String currency;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date date;
    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Participant> expenseParticipants;
    @NotNull
    private String type;

    /**
     * constructor for Expense class
     * @param expenseAuthor
     * @param purpose
     * @param amount
     * @param currency
     * @param expenseParticipants
     * @param type
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
    public long getExpenseID() {
        return expenseID;
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

    public void setExpenseID(long expenseID) {
        this.expenseID = expenseID;
    }

    /**
     * equals method
     * @param o
     * @return true if equals, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return expenseID == expense.expenseID
                && Double.compare(expense.amount, amount) == 0
                && Objects.equals(expenseAuthor, expense.expenseAuthor)
                && Objects.equals(purpose, expense.purpose)
                && Objects.equals(currency, expense.currency)
                && Objects.equals(date, expense.date)
                && Objects.equals(expenseParticipants, expense.expenseParticipants)
                && Objects.equals(type, expense.type);
    }

    /**
     * hashCode method
     * @return an hashCode for a specific object
     */
    @Override
    public int hashCode() {
        return Objects.hash(expenseID, expenseAuthor, purpose,
                amount, currency, date, expenseParticipants, type);
    }
}
