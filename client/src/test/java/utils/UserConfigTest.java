package utils;

import client.utils.UserConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UserConfigTest {
    /**
     * Assert that the UserConfig instance is created
     */
    @Test
    void createInstance() throws IOException {
        assertNotNull(UserConfig.createInstance());
    }

    /**
     * Assert that it initializes with the correct URL
     */
    @Test
    void getUrl() throws IOException {
        UserConfig userConfig = UserConfig.createInstance();
        assertEquals("http://localhost:8080/", userConfig.getUrl());
    }

    /**
     * Assert that it initializes with the correct locale
     */
    @Test
    void getLocale() throws IOException {
        UserConfig userConfig = UserConfig.createInstance();
        assertEquals("en", userConfig.getLocale());
    }

    /**
     * Assert that it changes the locale
     */
    @Test
    void setLocale() throws IOException {
        UserConfig userConfig = UserConfig.createInstance();
        assertEquals("en", userConfig.getLocale());
        userConfig.setLocale("nl");
        assertEquals("nl", userConfig.getLocale());
        userConfig.setLocale("en"); // revert the setting
    }
}