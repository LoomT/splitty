package client.utils;

import javafx.scene.control.Alert;

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
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Config file not found.\nIf the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException("Config file not found");
        }
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Config file not found.\nIf the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException(e);
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
