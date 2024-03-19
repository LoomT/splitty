package client.scenes;

import client.components.EventListItemAdmin;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminOverviewCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private UserConfig userConfig;
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
        this.initialDirectory = new FileChooser().getInitialDirectory();
    }


    /**
     * This method is called when the fxml is loaded
     *
     */
    @FXML
    private void initialize() {
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Method to handle the refresh button click
     *
     */
    @FXML
    private void refreshButtonClicked() {
        loadAllEvents(password);
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
     *
     */
    public void loadAllEvents(String password) {
        List<Event> allEvents = server.getEvents(password);
        List<EventListItemAdmin> list = new ArrayList<>();

        eventList.getChildren().clear();


        for (int i = 0; i < allEvents.size(); i++) {
            int finalI = i;
            list.add(
                new EventListItemAdmin(
                    allEvents.get(i).getTitle(),
                    allEvents.get(i).getId(),
                    () -> {
                        eventList.getChildren().remove(list.get(finalI));
                    },
                    () -> {
                        // download the event json
                        Event event = allEvents.get(finalI);
                        FileChooser fileChooser = new FileChooser();
                        FileChooser.ExtensionFilter extensionFilter =
                                new FileChooser.ExtensionFilter("JSON files", "*.json");
                        fileChooser.getExtensionFilters().add(extensionFilter);
                        fileChooser.setInitialDirectory(initialDirectory);

                        File file = mainCtrl.showSaveFileDialog(fileChooser);
                        if(file == null) {
                            System.out.println("Selected file is null");
                            return;
                        }
                        // Save the file directory the file was saved in
                        // to be used next time for better UX
                        initialDirectory = file.getParentFile();

                        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                            ObjectWriter ow = new ObjectMapper().writer()
                                    .withDefaultPrettyPrinter();
                            String json = ow.writeValueAsString(event);
                            writer.write(json);
                        } catch (IOException e) {
                            System.out.println("Failed to save the event");
                        }
                    },
                    () -> {
                        // TODO display the event
                    }));
            eventList.getChildren().add(list.get(i));
        }

    }


}
