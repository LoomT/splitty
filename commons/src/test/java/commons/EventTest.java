package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class EventTest {

    @Test
    public void EventGetterTest(){
        String title = "title";
        Event test = new Event(1, title, null);
        assertEquals(1, test.getEventID());
        assertEquals("title", test.getTitle());
    }

    @Test
    public void EventSetterTest(){
        String title = "title";
        Event test = new Event(1, title, null);
        assertEquals("title", test.getTitle());
        test.setTitle("newTitle");
        assertEquals("newTitle", test.getTitle());
    }

    @Test
    public void ParticipantsAddTest(){
        Event test = new Event(1, "title", null);
        test.addParticipant("participant");
        List<String> list = new ArrayList<String>();
        list.add("participant");
        Event compare = new Event(1, "title", list);
    }
}
