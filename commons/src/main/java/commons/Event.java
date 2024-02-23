package commons;
import java.util.*;

public class Event {
    /*
      Properties:
      Int EventID to join an event (getter + set once in constructor)
      String title to easily differentiate two events (getter and setter)
      List<Participants> to store all active participants of an event (get, remove, edit and add method)
      Date creationDate to store the date of creation (getter + set once in constructor)

      Methods:
      Constructor to create an event
      getters for eventID, title and creationDate
      setter for title
      get, remove, edit and add method for participants


      */
    private final int eventID;

    private String title;

    private ArrayList<String> participants;

    private final Date creationDate;

    //Constructor:
    // Takes parameters:
    //  - int eventID
    //  - String title
    //  - List<String> list of participants
    // returns new Event
    public Event(int eventID, String title, List<String> participants){
        this.eventID = eventID;
        this.title = title;
        if(participants == null) this.participants = new ArrayList<String>();
        else this.participants = CopyLtoAL(participants);
        this.creationDate = new Date();
    }

    public ArrayList<String> CopyLtoAL(List<String> participants){
        ArrayList<String> result = new ArrayList<String>();
        for(String x : participants){
            result.add(x);
        }
        return result;
    }

    public int getEventID(){
        return this.eventID;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ArrayList<String> getParticipants(){
        return participants;
    }

    public String getParticipant(int index){
        if(participants == null || participants.size() <= index) return null;
        else return participants.get(index);
    }

    public void editParticipant(int index, String editedParticipant){
        if(participants == null || participants.size() <= index) throw new IndexOutOfBoundsException();
        participants.set(index, editedParticipant);
    }

    public void removeParticipant(int index){
        if(participants == null || participants.size() <= index) throw new IndexOutOfBoundsException();
        participants.remove(index);
    }

    public void addParticipant(String newParticipant){
        participants.add(newParticipant);
    }

    @Override
    public boolean equals(Object other) {
        if(other == null || this.getClass() != other.getClass()) return false;
        Event that = (Event) other;
        if(this.participants.size() != that.participants.size()) return false;
        for(int i = 0; i < this.participants.size(); i++){
            if(this.participants.get(i).equals(that.participants.get(i))) return false;
        }
        return this.eventID == that.eventID;
    }
}
