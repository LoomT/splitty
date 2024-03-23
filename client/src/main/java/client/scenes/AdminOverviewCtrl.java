package client.scenes;

import client.components.EventListItemAdmin;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminOverviewCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private final UserConfig userConfig;
    private File initialDirectory;

    @FXML
    private VBox eventList;
    private String password;

    @FXML
    private ChoiceBox<String> orderByChoiceBox;

    private LanguageConf languageConf;
    private List<Event> allEvents;

    /**
     * adminOverview screen controller constructor
     *
     * @param server       utils
     * @param mainCtrl     main scene controller
     * @param userConfig   the user configuration
     * @param languageConf the languageconf instance
     */
    @Inject
    public AdminOverviewCtrl(
            ServerUtils server,
            MainCtrl mainCtrl,
            UserConfig userConfig,
            LanguageConf languageConf
    ) {

        this.server = server;
        this.mainCtrl = mainCtrl;
        this.userConfig = userConfig;
        this.initialDirectory = userConfig.getInitialExportDirectory();
        this.languageConf = languageConf;
    }


    /**
     * This method is called when the fxml is loaded
     */
    @FXML
    private void initialize() {
        orderByChoiceBox.getItems().add(languageConf.get("AdminOverview.creationDate"));
        orderByChoiceBox.getItems().add(languageConf.get("AdminOverview.eventName"));
        orderByChoiceBox.getItems().add(languageConf.get("AdminOverview.numOfParticipants"));
        orderByChoiceBox.setValue(languageConf.get("AdminOverview.creationDate"));
        orderByChoiceBox.setOnAction((e1) -> {
            orderAndDisplayEvents();
        });

    }

    private void orderAndDisplayEvents() {
        List<EventListItemAdmin> list = new ArrayList<>();

        switch (orderByChoiceBox.getSelectionModel().getSelectedIndex()) {
            case 0: // Order by creation date
                allEvents.sort(((o1, o2) -> -o1.getCreationDate().compareTo(o2.getCreationDate())));
                break;
            case 1: // Order by event name
                allEvents.sort(Comparator.comparing(o -> o.getTitle().toLowerCase()));
                break;
            case 2: // order by num of participants
                allEvents.sort(Comparator.comparingInt(o -> -o.getParticipants().size()));
                break;
        }


        eventList.getChildren().clear();

        for (int i = 0; i < allEvents.size(); i++) {
            int finalI = i;
            list.add(
                    new EventListItemAdmin(
                            allEvents.get(i).getTitle(),
                            allEvents.get(i).getId(),
                            () -> {
                                int status = server.deleteEvent(allEvents.get(finalI).getId());
                                if (status != 204) {
                                    System.out.println("Server did not delete the event " + status);
                                    // TODO maybe trow an error message or smth
                                }
                                allEvents.remove(finalI);
                                eventList.getChildren().remove(list.get(finalI));
                            },
                            () -> eventExportHandler(allEvents.get(finalI)),
                            () -> {
                                // TODO display the event
                            }));
            eventList.getChildren().add(list.get(i));
        }
    }

    /**
     * @param password the admin password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Method to handle the refresh button click
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
        allEvents = server.getEvents(password);
        orderAndDisplayEvents();
    }

    /**
     * Prompts the user with the file chooser
     * and exports the event in JSON to the selected file
     *
     * @param event event to export
     */
    public void eventExportHandler(Event event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter =
                new FileChooser.ExtensionFilter("JSON files", "*.json");
        fileChooser.getExtensionFilters().add(extensionFilter);
        if (initialDirectory != null && initialDirectory.exists())
            fileChooser.setInitialDirectory(initialDirectory);
        else initialDirectory = null;

        File file = mainCtrl.showSaveFileDialog(fileChooser);
        if (file == null) {
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
}
