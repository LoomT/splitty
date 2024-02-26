package client.utils;


import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton pattern
 */
public class ConfigParser {

    private static ConfigParser parser;
    private Properties configProperties;

    /**
     * The constructor is private so multiple instances can't be created
     */
    private ConfigParser() throws IOException {
        configProperties = new Properties();
        configProperties.load(new FileInputStream(Objects.requireNonNull(ConfigParser.class.getClassLoader()
                .getResource("client/config.properties")).getPath()));
    }


    /**
     * Creates an instance of the parser
     *
     * @return the config parser singleton instance
     */
    public static ConfigParser createInstance() throws IOException {
        if(parser == null) {
            parser = new ConfigParser();
        }
        return parser;
    }
    /**
     * Returns the server URL from the config
     *
     * @return the server URL
     */
    public String getUrl() {
        return configProperties.getProperty("serverURL");
    }
}
