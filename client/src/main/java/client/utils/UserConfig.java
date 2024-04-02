package client.utils;

import com.google.inject.Inject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton design pattern
 */
public class UserConfig {
    private final Properties configProperties;
    private final IOInterface io;

    /**
     * The constructor which initializes properties from file, and opens a writer to the file
     *
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
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed language to " + lang);
        }
    }

    /**
     * @return directory the event was saved to the previous time
     */
    public File getInitialExportDirectory() {
        return new File(configProperties.getProperty("initialExportDirectory",  ""));
    }

    /**
     * @param directory file directory to save the path of
     */
    public void setInitialExportDirectory(File directory) {
        configProperties.setProperty("initialExportDirectory", directory.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed export dir to " + directory.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save config when updating export directory");
        }
    }

    /**
     * @return the codes for the recently accessed events
     */
    public List<String> getRecentEventCodes() {
        String codes = configProperties.getProperty("recentEventCodes", "");
        if (codes.isEmpty()) {
            return new ArrayList<>();
        }
        Scanner scanner = new Scanner(codes);
        scanner.useDelimiter(",");
        List<String> recentCodes = new ArrayList<>();
        while (scanner.hasNext()) {
            String code = scanner.next();
            if (code.length() != 5) {
                System.out.println("Incorrectly read code: " + code);
                continue;
            }
            recentCodes.add(code);
        }
        return recentCodes;
    }

    /**
     * @param code the 5 letter code of the event to store in the config file
     */
    public void setMostRecentEventCode(String code) {
        System.out.println("Writing code " + code);
        List<String> currentCodes = getRecentEventCodes();
        currentCodes.remove(code);
        currentCodes.addFirst(code);
        StringBuilder strToWrite = new StringBuilder();
        for (int i = 0; i < currentCodes.size(); i++) {
            String curr = currentCodes.get(i);
            strToWrite.append(curr);
            if (i < currentCodes.size() - 1) {
                strToWrite.append(",");
            }
        }
        configProperties.setProperty("recentEventCodes", strToWrite.toString());
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Set most recent event code: " + code);
        } catch (Exception e) {
            System.out.println("Something went wrong while writing to the config file.");
        }
    }
}
