package client.utils;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton design pattern
 */
public class UserConfig {

    private static UserConfig config;
    private final String configPath = Objects.requireNonNull(UserConfig.class.getClassLoader()
            .getResource("client/config.properties")).getPath();
    private final Properties configProperties;

    /**
     * The constructor is private so multiple instances can't be created
     */
    private UserConfig() throws IOException {
        configProperties = new Properties();
        configProperties.load(new FileInputStream(configPath));
    }
    /**
     * Creates an instance of the parser
     *
     * @return the config parser singleton instance
     */
    public static UserConfig createInstance() throws IOException {
        if(config == null) {
            config = new UserConfig();
        }
        return config;
    }
    /**
     * Returns the server URL from the config
     *
     * @return the server URL
     */
    public String getUrl() {
        return configProperties.getProperty("serverURL");
    }

    /**
     * Returns previously set locale from the config
     *
     * @return locale
     */
    public String getLocale() {
        return configProperties.getProperty("lang");
    }

    /**
     * Saves the locale to config file
     *
     * @param lang locale to save
     * @throws IOException if config file can not be accessed
     */
    public void setLocale(String lang) throws IOException {
        configProperties.setProperty("lang", lang);
        configProperties.store(new FileOutputStream(configPath), "Changed language to " + lang);
    }
}
