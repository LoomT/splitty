package client.scenes;

import client.components.EventListItemAdmin;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminOverviewCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private final UserConfig userConfig;
    private File initialDirectory;

    @FXML
    private VBox eventList;
    private String password;


    /**
     * adminOverview screen controller constructor
     *
     * @param server   utils
     * @param mainCtrl main scene controller
     * @param userConfig the user configuration
     */
    @Inject
    public AdminOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, UserConfig userConfig) {

        this.server = server;
        this.mainCtrl = mainCtrl;
        this.userConfig = userConfig;
        this.initialDirectory = userConfig.getInitialExportDirectory();
    }


    /**
     * This method is called when the fxml is loaded
     *
     */
    @FXML
    private void initialize() {
    }

    /**
     * @param password the admin password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Method to handle the refresh button click
     *
     */
    @FXML
    private void refreshButtonClicked() {
        loadAllEvents();
    }


    /**
     * Show admin login screen when back button is clicked
     */
    @FXML
    private void backButtonClicked() {
        mainCtrl.showAdminLogin();
    }



    /**
     * Method to get all the events into the list
     */
    public void loadAllEvents() {
        List<Event> allEvents = server.getEvents(password);
        List<EventListItemAdmin> list = new ArrayList<>();

        eventList.getChildren().clear();

        for (int i = 0; i < allEvents.size(); i++) {
            int finalI = i;
            list.add(
                new EventListItemAdmin(
                    allEvents.get(i).getTitle(),
                    allEvents.get(i).getId(),
                    () -> eventList.getChildren().remove(list.get(finalI)),
                    () -> eventExportHandler(allEvents.get(finalI)),
                    () -> {
                        // TODO display the event
                    }));
            eventList.getChildren().add(list.get(i));
        }

    }

    /**
     * Initializes the file chooser with json extension filter
     * and the initial directory
     *
     * @return file chooser
     */
    private @NotNull FileChooser initFileChooser() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter =
                new FileChooser.ExtensionFilter("JSON files", "*.json");
        fileChooser.getExtensionFilters().add(extensionFilter);
        if(initialDirectory != null && initialDirectory.exists())
            fileChooser.setInitialDirectory(initialDirectory);
        else initialDirectory = null;
        return fileChooser;
    }

    /**
     * Prompts the user with the file chooser
     * and exports the event in JSON to the selected file
     *
     * @param event event to export
     */
    public void eventExportHandler(Event event) {
        FileChooser fileChooser = initFileChooser();

        File file = mainCtrl.showSaveFileDialog(fileChooser);
        if(file == null) {
            System.out.println("No file selected");
            return;
        }
        // Save the file directory the file was saved in
        // to be used next time for better UX
        initialDirectory = file.getParentFile();
        // persist the export directory
        userConfig.setInitialExportDirectory(initialDirectory);

        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(event);
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Failed to save the event");
        }
    }

    /**
     * Opens the file chooser and imports the selected JSON files as events
     */
    @FXML
    private void importButtonClicked() {
        FileChooser fileChooser = initFileChooser();

        List<File> files = mainCtrl.showOpenMultipleFileDialog(fileChooser);

        if(files == null) {
            System.out.println("No files selected");
            return;
        }
        // Get distinct parent directory of opened files
        List<File> parents = files.stream().map(File::getParentFile).distinct().toList();
        // If all files were opened from the same directory
        // save that file directory to be used next time for better UX
        if(parents.size() == 1) {
            initialDirectory = parents.getFirst();
            // persist the directory
            userConfig.setInitialExportDirectory(initialDirectory);
        }

        ObjectReader reader = new ObjectMapper().reader().forType(Event.class);
        for(File file : files) {
            try {
                Event event = reader.readValue(file);
                server.importEvent(password, event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        loadAllEvents();
    }
}
