package commons;

import java.io.Serializable;
import java.util.Objects;

public class EventWeakKey implements Serializable {
    private String eventID;
    private long id;

    /**
     * No arg constructor for JPA
     */
    public EventWeakKey() {
    }

    /**
     * @param eventID event id
     * @param id item id
     */
    public EventWeakKey(String eventID, long id) {
        this.eventID = eventID;
        this.id = id;
    }

    /**
     * @return event id
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * @return item id
     */
    public long getId() {
        return id;
    }

    /**
     * @param o other object
     * @return true iff equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventWeakKey that = (EventWeakKey) o;
        return id == that.id && Objects.equals(eventID, that.eventID);
    }

    /**
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(eventID, id);
    }

    /**
     * @return event and item ids in string form
     */
    @Override
    public String toString() {
        return "EventWeakKey{" +
                "eventID='" + eventID + '\'' +
                ", id=" + id +
                '}';
    }
}
