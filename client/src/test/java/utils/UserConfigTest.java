package utils;

import client.utils.IOInterface;
import client.utils.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

class UserConfigTest {
    TestIO IO; // dummy IO interface
    UserConfig userConfig;

    @BeforeEach
    void setUp() throws IOException {
        Writer writer = new StringWriter();
        writer.write("""
                serverURL=http://localhost:8080/
                lang=en""");
        IO = new TestIO(writer);
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
    void getUrl() throws IOException {
        assertEquals("http://localhost:8080/", userConfig.getUrl());
    }

    /**
     * Assert that it initializes with the correct locale
     */
    @Test
    void getLocale() throws IOException {
        assertEquals("en", userConfig.getLocale());
    }

    /**
     * Assert that it changes the locale
     */
    @Test
    void setLocale() throws IOException {
        assertEquals("en", userConfig.getLocale());
        assertFalse(IO.getWriter().toString().contains("nl"));
        userConfig.setLocale("nl");
        assertEquals("nl", userConfig.getLocale());
        assertTrue(IO.getWriter().toString().contains("nl"));
    }
}