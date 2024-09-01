package client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton design pattern
 */
public class UserConfig {
    private final Properties configProperties;
    private final IOInterface io;
    private Runnable callback;

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
        return configProperties.getProperty("serverURL", "localhost:8080");
    }

    /**
     * @param url server URL
     * @throws IOException if config file can not be accessed
     */
    public void setURL(String url) throws IOException {
        configProperties.setProperty("serverURL", url);
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed server to " + url);
        }
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
     * Deletes the event code from the list of recent event codes
     *
     * @param code the 5-letter code of the event to delete from the config file
     */
    public void deleteEventCode(String code) {
        List<String> currentCodes = getRecentEventCodes();
        currentCodes.remove(code);
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
            configProperties.store(writer, "Deleted the following event code: " + code);
        } catch (Exception e) {
            System.out.println("Something went wrong while writing to the config file.");
        }
    }

    /**
     * @param code the 5-letter code of the event to store in the config file
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

    /**
     * @return currently set preferred currency
     */
    public String getCurrency() {
        return configProperties.getProperty("currency", "EUR");
    }

    /**
     * @param currency newly set preferred currency
     */
    public void setCurrency(String currency) throws IOException {
        configProperties.setProperty("currency", currency);
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed currency to " + currency);
        }
    }

    /**
     * @return true if high contrast is enabled
     */
    public boolean getHighContrast() {
        return Boolean.parseBoolean(configProperties.getProperty("highContrast", "false"));
    }

    /**
     * Does not persist the contrast, reloads the fxml files with the new setting
     *
     * @param highContrast high contrast bool
     */
    public void setHighContrast(boolean highContrast) {
        configProperties.setProperty("highContrast", highContrast ? "true" : "false");

        callback.run();
    }

    /**
     * Persists the currently set contrast option
     *
     * @throws IOException if config file can not be accessed
     */
    public void persistContrast() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed high contrast to "
                    + (Boolean.parseBoolean(configProperties.get("highContrast")
                    .toString()) ? "true" : "false"));
        }
    }

    /**
     * @param function to callback when contrast changes
     */
    public void onContrastChange(Runnable function) {
        callback = function;
    }

    /**
     * Username of the mail address
     * @return mail password in the config file
     */
    public String getUsername(){
        return (String) configProperties.get("spring.mail.username");
    }

    /**
     * Password of the mail address
     * @return mail password in the config file
     */
    public String getMailPassword(){
        return (String) configProperties.get("spring.mail.password");
    }

    /**
     * host in the config file for the MailService
     * @return host
     */
    public String getHost(){
        return (String) configProperties.get("spring.mail.host");
    }

    /**
     * port in the config file for the MailService
     * @return port
     */
    public int getPort(){
        return Integer.parseInt((String)configProperties.get("spring.mail.port"));
    }

    /**
     * return properties needed in the JavaMailSender
     * @return properties to initialise the MailService
     */
    public Properties getMailProperties(){
        Properties result = new Properties();
        result.setProperty("mail.smtp.auth",
                configProperties.getProperty("mail.smtp.auth"));
        result.setProperty("mail.smtp.starttls.enable",
                configProperties.getProperty("mail.smtp.starttls.enable"));
        return result;
    }

    /**
     * sets username in the user config
     * @param username username
     */
    public void setUsername(String username) throws IOException {
        configProperties.setProperty("spring.mail.username", username);
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed email username to " + username);
        }
    }

    /**
     * sets password in the user config
     * @param password password
     */
    public void setMailPassword(String password) throws IOException {
        configProperties.setProperty("spring.mail.password", password);
        try (BufferedWriter writer = new BufferedWriter(io.write())) {
            configProperties.store(writer, "Changed email password to " + password);
        }
    }

    /**
     * @return supported locales
     */
    public List<Locale> getSupportedLocales() {
        List<String> result;
        ObjectReader reader = new ObjectMapper().reader().forType(ArrayList.class);
        try {
            result = reader.readValue((String) configProperties.get("locales"));
            return result.stream().map(Locale::of).toList();
        } catch (JsonProcessingException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The property locales in config.properties is not formatted properly. " +
                            "Please make sure it's similar to 'locales=[\"en\", \"nl\"]'");
            alert.setHeaderText("Config parse error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException();
        }
    }
}
