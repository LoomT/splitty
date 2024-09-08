package client.utils;

import javafx.scene.control.Alert;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Scanner;

public class FileIO implements IOInterface{
    private final File file;

    /**
     * @param url the URL of the config file
     */
    public FileIO(@Nullable InputStream url) {
        if(url == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Config file not found.\nIf the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException("Config file not found");
        }
//        file = new File(URLDecoder.decode(url, StandardCharsets.UTF_8));
        try {
            AppDirs appDirs = AppDirsFactory.getInstance();
            String appData = appDirs.getUserDataDir("Splitty", null, null);
            File dir = new File(appData);
            System.out.println(dir.getPath());
            dir.mkdirs();
            if(!dir.exists()) {
                System.out.println("Cannot write to user data directory");
                throw new RuntimeException("Cannot write to user data directory");
            }
            file = new File(appData, "config.properties");
            if(file.createNewFile()) {
                StringBuilder result = new StringBuilder();
                try (Scanner scanner = new Scanner(url)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        result.append(line).append("\n");
                    }
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(result.toString());
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(!file.exists()) {
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
