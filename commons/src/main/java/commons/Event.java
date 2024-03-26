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
    private Date creationDate;

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
    public Event(@NotNull String title) {
        this();
        this.title = title;
        this.participants = new ArrayList<>();
        this.expenses = new ArrayList<>();
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
            clone.creationDate = (Date) this.creationDate.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
