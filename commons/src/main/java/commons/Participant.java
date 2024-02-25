package commons;
import jakarta.persistence.*;

import java.util.*;


@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

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
        return id;
    }

    /**
     * setter for id
     * @param id id to be changed to
     */
    public void setID(long id){
        this.id = id;
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

        if (id != that.id) return false;
        return Objects.equals(name, that.name);
    }

    /**
     * hashCode generator for class Participant
     * @return a unique hashcode for the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

