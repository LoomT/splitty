package commons;
import java.util.*;
import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {
    /*
      Properties:
      Int EventID to join an event (getter + set once in constructor)
      String title to easily differentiate two events (getter and setter)
      List<Participants> to store all
      active participants of an event (get, remove, edit and add method)
      Date creationDate to store the date of creation (getter + set once in constructor)

      Methods:
      Constructor to create an event
      getters for eventID, title and creationDate
      setter for title
      unique event ID generator
      get, remove, edit and add method for participants
      equals method
      hashing method

      */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eventID;

    @Column(name = "title", nullable = false)
    private String title;

    @ElementCollection
    @CollectionTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "participant", nullable = false)
    private List<String> participants;
//    private ArrayList<Expense> expenses;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private final Date creationDate;

    /**
     * No-Argument Constructor
     * Required by JPA
     */

    public Event() {
        this.creationDate = new Date();
    }

    /**
     * Constructor that does take arguments, uses this()
     *
     * @param title string
     * @param participants list of strings (going to be
     *                     participant objects in the future)
     */

    public Event(String title, List<String> participants) {
        this();
        this.title = title;
        this.participants = Objects.requireNonNullElseGet(participants, ArrayList::new);
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

    public boolean deleteParticipant(String participant){
        return getParticipants().remove(participant);
    }

    /**
     * Adds a participant to the list of participants
     *
     * @param participant String (In the future probably a participant object)
     */

    public void addParticipant(String participant){
        this.participants.add(participant);
    }

    /**
     * getter for Participants
     *
     * @return participants
     */

    public List<String> getParticipants() {
        return participants;
    }

    /**
     * Setter for participants
     *
     * @param participants list of participants
     */

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    /**
     * Equals method that checks whether two instances are equal
     * Does not take the unique eventID into consideration
     *
     * @param obj another object
     * @return boolean value
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Event other = (Event) obj;
        return Objects.equals(title, other.title) &&
                Objects.equals(participants, other.participants); // &&
                // Objects.equals(creationDate, other.creationDate);
    }

    /**
     * HashCode generator
     *
     * @return representation of object as an integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, participants, creationDate);
    }
}
