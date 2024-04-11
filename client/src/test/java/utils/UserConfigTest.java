package utils;

import client.utils.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserConfigTest {
    TestIO IO; // dummy IO interface
    UserConfig userConfig;

    @BeforeEach
    void setUp() throws IOException {
        IO = new TestIO("""
                serverURL=localhost:8080
                lang=en
                recentEventCodes=hello,there""");
        userConfig = new UserConfig(IO);
    }

    /**
     * Assert that the UserConfig instance is created
     */
    @Test
    void instanceNotNull() {
        assertNotNull(userConfig);
    }

    /**
     * Assert that it initializes with the correct URL
     */
    @Test
    void getUrl() {
        assertEquals("localhost:8080", userConfig.getUrl());
    }

    /**
     * Assert that it initializes with the correct locale
     */
    @Test
    void getLocale() {
        assertEquals("en", userConfig.getLocale());
    }

    /**
     * Assert that it changes the locale
     */
    @Test
    void setLocale() throws IOException {
        assertEquals("en", userConfig.getLocale());
        assertFalse(IO.getContent().contains("nl"));
        userConfig.setLocale("nl");
        assertEquals("nl", userConfig.getLocale());
        assertTrue(IO.getContent().contains("nl"));
    }

    /**
     * tests if the config can correctly read and parse the event codes
     */
    @Test
    void getRecentEventCodes() {
        List<String> eventCodes = List.of("hello", "there");
        assertEquals(eventCodes, userConfig.getRecentEventCodes());
    }

    /**
     * tests the behavior of updating the event codes
     */
    @Test
    void setMostRecentEventCode() {
        List<String> eventCodes = List.of("world", "hello", "there");
        userConfig.setMostRecentEventCode("world");
        assertEquals(eventCodes, userConfig.getRecentEventCodes());
        List<String> eventCodes2 = List.of("hello", "world", "there");
        userConfig.setMostRecentEventCode("hello");
        assertEquals(eventCodes2, userConfig.getRecentEventCodes());

    }
}