package client.utils;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class FileIO implements IOInterface{
    private final File file;

    /**
     * @param url the URL of the config file
     */
    public FileIO(@Nullable URL url) {
        if(url == null) {
            //TODO replace crash with a pop up
            throw new RuntimeException("Config file not found");
        }
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            //TODO replace crash with a pop up or not
            throw new RuntimeException(e);
        }
    }

    /**
     * second constructor to insert a URI directly instead of URL
     * @param path the uri of the config file
     */
    public FileIO(@Nullable String path){
        if(path == null) {
            //TODO replace crash with a pop up
            throw new RuntimeException("Config file not found");
        }
        file = new File(path);
    }

    /**
     * @return file reader
     */
    @Override
    public Reader read() throws FileNotFoundException {
        return new FileReader(file);
    }

    /**
     * @return file writer
     */
    @Override
    public Writer write() throws IOException {
        return new FileWriter(file);
    }
}
