package client;


import java.io.*;
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
    public static ConfigParser createInstance() throws IOException {
        if(parser == null) {
            List<String> settings = readConfig();
            parser = new ConfigParser(settings.get(0));
        }
        return parser;
    }


    /**
     * Reads the settings of the config and returns them in a list
     * If the config file does not exist, creates one with default values
     *
     * @return the list of settings
     * @throws IOException if file can not be accessed
     */
    private static List<String> readConfig() throws IOException {
        File file = new File("../resources/config.txt");
        if(!file.exists()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("http://localhost:8080/");
            writer.flush();
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return reader.lines().toList();
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
