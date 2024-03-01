package client.utils;

import com.google.inject.Inject;

import java.io.*;
import java.util.Properties;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton design pattern
 */
public class UserConfig {
    private final Properties configProperties;
    private final IOInterface io;

    /**
     * The constructor which initializes properties from file, and opens a writer to the file
     * @param io input output interface for config file
     */
    @Inject
    public UserConfig(IOInterface io) throws IOException {
        configProperties = new Properties();
        configProperties.load(new BufferedReader(io.read()));
        this.io = io;
    }

    /**
     * Returns the server URL from the config
     *
     * @return the server URL
     */
    public String getUrl() {
        return configProperties.getProperty("serverURL", "http://localhost:8080/");
    }

    /**
     * Returns previously set locale from the config
     *
     * @return locale
     */
    public String getLocale() {
        return configProperties.getProperty("lang", "en");
    }

    /**
     * Saves the locale to config file
     *
     * @param lang locale to save
     * @throws IOException if config file can not be accessed
     */
    public void setLocale(String lang) throws IOException {
        configProperties.setProperty("lang", lang);
        try(BufferedWriter writer = new BufferedWriter(io.write()) ) {
            configProperties.store(writer, "Changed language to " + lang);
        }
    }
}
