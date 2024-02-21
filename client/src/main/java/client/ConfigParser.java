package client;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton pattern
 */
public class ConfigParser {

    private static ConfigParser parser;
    private String url;

    /**
     * The constructor is private so multiple instances can't be created
     */
    private ConfigParser(String url) {
        this.url = url;
    }


    /**
     * Creates
     *
     * @return the config parser singleton instance
     */
    public static ConfigParser createInstance() {
        if(parser == null) {
            List<String> settings;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File("../resources/config.txt")));
                settings = reader.lines().toList();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            parser = new ConfigParser(settings.get(0));
        }
        return parser;
    }

    /**
     * Returns the server URL from the config
     *
     * @return the server URL
     */
    public String getUrl() {
        return url;
    }
}
