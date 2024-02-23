package commons;


import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Expense {
    /*
    Properties:
    Int expenseID so that one can reuse this type of expense
    String participant for the person that paid
    String purpose for the purpose of the existent expense
    Double amount for the amount paid by the participant
    String currency for the currency that was used
    Date date for the date when the expense was paid
    Boolean splitMethod, 0 - equally among all participant, 1 - only a part of them
    List<String> participants for all the people that are splitting the expense
    String type for the type of the current expense
     */

    private final int expenseID;
    private String participant;
    private String purpose;
    private double amount;
    private String currency;
    private final Date date;
    private List<String> participants;
    private String type;

    /**
     * constructor for Expense class
     * @param expenseID
     * @param participant
     * @param purpose
     * @param amount
     * @param currency
     * @param date
     * @param participants
     * @param type
     */
    public Expense(int expenseID, String participant, String purpose, double amount,
                   String currency, Date date, List<String> participants, String type) {
        this.expenseID = expenseID;
        this.participant = participant;
        this.purpose = purpose;
        this.amount = amount;
        this.currency = currency;
        this.date = new Date();
        this.participants = participants;
        this.type = type;
    }

    /**
     * getter for expenseID
     * @return the expenseID
     */
    public int getExpenseID() {
        return expenseID;
    }

    /**
     * getter for participant
     * @return the participant
     */
    public String getParticipant() {
        return participant;
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
    public List<String> getParticipants() {
        return participants;
    }

    /**
     * getter for type
     * @return the type of expense
     */
    public String getType() {
        return type;
    }

}
