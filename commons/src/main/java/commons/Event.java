package commons;
import java.util.*;

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
    private final int eventID;

    private String title;

    private List<String> participants;

//    private ArrayList<Expense> expenses;

    private final Date creationDate;

    /**
     * Constructor for an event instance
     *
     * @param title Event Title
     * @param participants list of participants
     */
    public Event(String title, List<String> participants){
        this.eventID = generateUniqueId();
        this.title = title;
        this.participants = participants;
        this.creationDate = new Date();
    }

//    /**
//     * Method to copy list to Array List
//     *
//     * @param participants list
//     * @return array list
//     */
//    public ArrayList<String> listToArrayList(List<String> participants){
//        ArrayList<String> result = new ArrayList<String>();
//        for(String x : participants){
//            result.add(x);
//        }
//        return result;
//    }


    /**
     * Getter for the unique EventID
     *
     * @return integer
     */
    public int getEventID(){
        return this.eventID;
    }

    private static int lastId = 9999;

    /**
     * Unique ID generator
     * Cannot generate unlimited uniqueIDs
     * @return integer
     */
    public static int generateUniqueId() {
        lastId = (lastId + 1) % 100000;
        if (lastId < 10000) {
            lastId = 10000;
        }
        return lastId;
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
     * Deletes a participant from the list of participants
     *
     * @param participant String (In the future probably a participant object)
     */

    public void deleteParticipant(String participant){
        this.participants.remove(participant);
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

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    /**
     * Equals method that checks whether two instances are equal
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
        return eventID == other.eventID &&
                Objects.equals(title, other.title) &&
                Objects.equals(participants, other.participants) &&
                Objects.equals(creationDate, other.creationDate);
    }

    /**
     * HashCode generator
     *
     * @return representation of object as an integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(eventID, title, participants, creationDate);
    }
}
