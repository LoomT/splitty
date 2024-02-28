package commons;

import jakarta.persistence.*;
import java.util.*;

/**
 * Class for testing purposes
 */
@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;

    /**
     * empty constructor
     */
    public Participant() {

    }

    /**
     * constructor for dummy class Participant
     *
     * @param name name
     */
    public Participant(String name) {
        this();
        this.name = name;
    }

    /**
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param o object
     * @return true iff equal ids
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return id == that.id;
    }

    /**
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * @return string
     */
    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
