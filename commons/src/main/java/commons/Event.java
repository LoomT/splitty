package commons;
import java.util.*;
import jakarta.persistence.*;

@Entity
public class Event {
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
    private String id;

    private String title;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Participant> participants;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Expense> expenses;
    @Temporal(TemporalType.TIMESTAMP)
    private final Date creationDate;

    /**
     * No-Argument Constructor
     * Required by JPA
     */
    public Event() {
        this.creationDate = new Date();
    }

    /**
     * Constructor with just the title
     *
     * @param title name of the event
     */
    public Event(String title) {
        this();
        this.title = title;
        this.participants = new ArrayList<>();
    }

    /**
     * Constructor that does take arguments, uses this()
     *
     * @param title name of the event
     * @param participants participants within an event
     * @param expenses expenses within the event
     *
     */
    public Event(String title, List<Participant> participants, List<Expense> expenses) {
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

        if (!Objects.equals(id, event.id) || !Objects.equals(title, event.title)) return false;
        if (!Objects.equals(participants, event.participants)) return false;
        if (!Objects.equals(expenses, event.expenses)) return false;
        return Objects.equals(creationDate, event.creationDate);
    }

    /**
     * HashCode generator
     *
     * @return representation of object as an integer value
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (expenses != null ? expenses.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
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
                '}';
    }
}
