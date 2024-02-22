package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


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
}
