package commons;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@IdClass(EventWeakKey.class)
public class Tag implements Cloneable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Id
    @Column(name = "event_id", length = 5, nullable = false)
    private String eventID;
    @ManyToOne
    private String name;
    @ManyToOne
    private String color;

    /**
     * constructor for tag all parameters
     * @param name
     * @param color
     */
    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    /**
     * getter for id
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * setter for id
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * getter for event id
     * @return event id
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * setter for event id
     * @param eventID the event id
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * getter for name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * setter for name
     * @param name the name of the tag
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter for color
     * @return the color of the tag
     */
    public String getColor() {
        return color;
    }

    /**
     * setter for color
     * @param color color of the tag
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * equals method
     * @param o
     * @return true if equals, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id == tag.id && Objects.equals(eventID, tag.eventID)
                && Objects.equals(name, tag.name) && Objects.equals(color, tag.color);
    }

    /**
     * hashcode method
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, eventID, name, color);
    }

    /**
     * to string method
     * @return string for tag
     */
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", eventID='" + eventID + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
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
    public Tag clone() {
        try {
            return (Tag) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    
}
