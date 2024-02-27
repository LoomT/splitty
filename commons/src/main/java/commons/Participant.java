package commons;
import jakarta.persistence.*;

import java.util.*;


@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long participantId;
    private String name;
    private String emailAddress;

    /**
     * constructor
     */
    public Participant(){
    }

    /**\
     * @param name name of participant
     */
    public Participant(String name) {
        this.name = name;
    }

    /**
     * getter for ID
     * @return the ID of the participant
     */
    public long getID() {
        return participantId;
    }

    /**
     * setter for id
     * @param id id to be changed to
     */
    public void setID(long id){
        this.participantId = id;
    }

    /**
     * getter for name
     * @return name of the participant
     */
    public String getName() {
        return name;
    }

    /**
     * setter for name
     * @param name name to replace the previous name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param o object to be compared to
     * @return true iff all the parameters are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

        if (participantId != that.participantId) return false;
        return Objects.equals(name, that.name);
    }

    /**
     * hashCode generator for class Participant
     * @return a unique hashcode for the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(participantId, name);
    }
}

