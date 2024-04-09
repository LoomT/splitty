package client.utils;

import javafx.scene.control.Alert;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FileIO implements IOInterface{
    private final File file;

    /**
     * @param url the URL of the config file
     */
    public FileIO(@Nullable URL url) {
        if(url == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Config file not found.\nIf the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException("Config file not found");
        }
        file = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
        if(file == null || !file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Config file not found.\nIf the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException("Config file not found");
        }
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
