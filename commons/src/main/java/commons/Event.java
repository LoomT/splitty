package commons;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Entity
// Index the title for faster sorting by title for admin,
@Table(indexes = {@Index(name = "idx_event_title", columnList = "title")
})
public class Event implements Cloneable {
    /*
      Properties:
      * Int EventID to join an event (getter + set once in constructor)
      * String title to easily differentiate two events (getter and setter)
      * List<Participants> to store all
        active participants of an event (get, remove, edit and add method)
      * List<Expenses> to store all
        active expenses of an event(get, remove, edit and add method)
      * Date creationDate to store the date of creation (getter + set once in constructor)

      Methods:
      * Constructor to create an event
      * getters for eventID, title and creationDate
      * setter for title
      * unique event ID generator
      * get, remove, edit and add method for participants, and expenses
      * equals method
      * hashing method

      */
    @Id
    @Column(nullable = false, length = 5)
    private String id;

    @Column(nullable = false)
    private String title;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "event_id", updatable = false, insertable = false)
    private List<Participant> participants;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "event_id", updatable = false, insertable = false)
    private List<Expense> expenses;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastActivity;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "event_id", updatable = false, insertable = false)
    private List<Transaction> transactions;

    /**
     * No-Argument Constructor
     * Required by JPA
     */
    public Event() {
        this.participants = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.creationDate = new Date();
        this.lastActivity = new Date();
    }

    /**
     * Constructor with just the title
     *
     * @param title name of the event
     */
    public Event(@NotNull String title) {
        this();
        this.title = title;
    }

    /**
     * Constructor that does take arguments, uses this()
     *
     * @param title name of the event
     * @param participants participants within an event
     * @param expenses expenses within the event
     *
     */
    public Event(@NotNull String title, List<Participant> participants, List<Expense> expenses) {
        this(title);
        this.participants = Objects.requireNonNullElseGet(participants, ArrayList::new);
        this.expenses = Objects.requireNonNullElseGet(expenses, ArrayList::new);
    }

    /**
     * @return id of the event
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the event
     *
     * @param id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for Title
     *
     * @return string
     */
    public String getTitle(){
        return this.title;
    }

    /**
     * Setter for title
     *
     * @param title string
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * getter for creation date
     *
     * @return the date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Delete participant function
     * @param participant takes a
     *                    participant (going to be an object in the future)
     * @return boolean value
     */
    public boolean deleteParticipant(Participant participant){
        return getParticipants().remove(participant);
    }

    /**
     * Adds a participant to the list of participants
     *
     * @param participant String (In the future probably a participant object)
     */
    public void addParticipant(Participant participant){
        this.participants.add(participant);
    }

    /**
     * getter for Participants
     *
     * @return participants
     */
    public List<Participant> getParticipants() {
        return participants;
    }

    /**
     * Setter for participants
     *
     * @param participants list of participants
     */
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    /**
     * delete method for expenses
     * @param expense expense to be removed
     * @return true iff removal was successful, false otherwise
     */
    public boolean deleteExpense(Expense expense){return expenses.remove(expense);}

    /**
     * getter for expenses
     *
     * @return expenses
     */
    public List<Expense> getExpenses() {
        return expenses;
    }

    /**
     * setter for expenses
     *
     * @param expenses list of expenses
     */
    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    /**
     * Adds a participant to the list of participants
     *
     * @param expense String (In the future probably a participant object)
     */
    public void addExpense(Expense expense){
        this.expenses.add(expense);
    }

    /**
     * @return last activity date
     */
    public Date getLastActivity() {
        return lastActivity;
    }

    /**
     * @param lastActivity last activity date
     */
    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    /**
     *
     * @param participant participant to be checked
     * @return true iff participant is in list, false otherwise
     */
    public boolean hasParticipant(Participant participant){
        return participants.contains(participant);
    }

    /**
     *
     * @param expense expense to be checked
     * @return true iff expense is in list, false otherwise
     */
    public boolean hasExpense(Expense expense){
        return expenses.contains(expense);
    }

    /**
     * @return transaction list
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * @param transactions transactions
     */
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * @param transaction transaction to add to the event
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Equals method that checks whether two instances are equal
     * Does not take the unique eventID into consideration
     *
     * @param o another object
     * @return boolean value
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id) && Objects.equals(title, event.title)
                && Objects.equals(participants, event.participants)
                && Objects.equals(expenses, event.expenses)
                && Objects.equals(creationDate, event.creationDate)
                && Objects.equals(lastActivity, event.lastActivity)
                && Objects.equals(transactions, event.transactions);
    }

    /**
     * HashCode generator
     *
     * @return representation of object as an integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title, participants, expenses, creationDate, lastActivity, transactions);
    }

    /**
     * @return a string representation of this
     */
    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", participants=" + participants +
                ", expenses=" + expenses +
                ", creationDate=" + creationDate +
                ", lastActivity=" + lastActivity +
                ", transactions=" + transactions +
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
    public Event clone() {
        try {
            Event clone = (Event) super.clone();
            clone.participants = new ArrayList<>(this.participants.size());
            for (Participant p : this.participants) {
                clone.participants.add(p.clone());
            }
            clone.expenses = new ArrayList<>(this.expenses.size());
            for (Expense e : this.expenses) {
                clone.expenses.add(e.clone());
            }
            for(Expense e : clone.expenses) {
                e.setExpenseAuthor(clone.participants.stream()
                        .filter(p -> p.getId() == e.getExpenseAuthor().getId())
                        .findAny().orElseThrow());
                Set<Long> ids = clone.participants.stream()
                        .map(Participant::getId).collect(Collectors.toSet());
                e.setExpenseParticipants(new ArrayList<>(clone.participants.stream()
                        .filter(p -> ids.contains(p.getId())).toList()));
            }
            clone.transactions = new ArrayList<>(this.transactions.size());
            for(Transaction t : this.transactions) {
                Transaction cloneTransaction = t.clone();
                cloneTransaction.setGiver(clone.participants.stream()
                        .filter(p -> p.getId() == cloneTransaction.getGiver().getId())
                        .findAny().orElseThrow());
                cloneTransaction.setReceiver(clone.participants.stream()
                        .filter(p -> p.getId() == cloneTransaction.getReceiver().getId())
                        .findAny().orElseThrow());
                clone.transactions.add(cloneTransaction);
            }
            clone.creationDate = (Date) this.creationDate.clone();
            clone.lastActivity = (Date) this.lastActivity.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
