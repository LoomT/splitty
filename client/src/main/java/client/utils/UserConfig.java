package client.utils;

import com.google.inject.Inject;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.Properties;

/**
 * Interacts with the config file and stores the settings
 * Follows singleton design pattern
 */
public class UserConfig {
    private final Properties configProperties;
    private final BufferedWriter writer;

    /**
     * The constructor is private so multiple instances can't be created
     */
    @Inject
    public UserConfig(Reader reader, Writer writer) throws IOException {
        File file = new File(UserConfig.class.getClassLoader().getResource("client/config.properties").getPath());
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        configProperties = new Properties();
        configProperties.load(new BufferedReader(reader));
//        configProperties.load(new BufferedReader(new FileReader(UserConfig.class.getClassLoader().getResource("client/config.properties").getFile())));
        this.writer = new BufferedWriter(writer);
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
        configProperties.store(writer, "Changed language to " + lang);
    }
}
