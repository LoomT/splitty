package client;

import client.utils.ConfigParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {

    ConfigParser configParser;

    {
        try {
            configParser = ConfigParser.createInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create an instance of config parser", e);
        }
    }

    /**
     * Reset the config parser to default properties before each test
     */
    @BeforeEach
    void setUp() {
        try {
            configParser.setLocale("en");
        } catch (IOException e) {
            throw new RuntimeException("Failed to access the file while resetting locale",e);
        }
    }
    /**
     * Assert that the parser is created
     */
    @Test
    void createInstance() throws IOException {
        assertNotNull(ConfigParser.createInstance());
    }

    /**
     * Assert that it initializes with the correct URL
     */
    @Test
    void getUrl() throws IOException {
        ConfigParser parser = ConfigParser.createInstance();
        assertEquals("http://localhost:8080/", parser.getUrl());
    }

    @Test
    void getLocale() throws IOException {
        ConfigParser parser = ConfigParser.createInstance();
        assertEquals("en", parser.getLocale());
    }

    @Test
    void setLocale() throws IOException {
        ConfigParser parser = ConfigParser.createInstance();
        assertEquals("en", parser.getLocale());
        parser.setLocale("nl");
        assertEquals("nl", parser.getLocale());
    }
}