package client.scenes;

import client.components.Confirmation;
import client.MockClass.MainCtrlInterface;
import client.components.EventListItemAdmin;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import commons.Event;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ConnectException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AdminOverviewCtrl {

    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;

    private final UserConfig userConfig;
    private File initialDirectory;

    @FXML
    private VBox eventList;
    private String password;

    @FXML
    private ChoiceBox<String> orderByChoiceBox;
    @FXML
    private Button backButton;

    @FXML
    private CheckBox reverseOrderCheckBox;

    private final LanguageConf languageConf;
    private List<Event> allEvents;

    private Thread poller;

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
            MainCtrlInterface mainCtrl,
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
        orderByChoiceBox.getItems().add(languageConf.get("AdminOverview.lastActivity"));
        orderByChoiceBox.setValue(languageConf.get("AdminOverview.creationDate"));
        orderByChoiceBox.setOnAction((e1) -> orderAndDisplayEvents());

        reverseOrderCheckBox.setOnAction((e1) -> orderAndDisplayEvents());
    }

    private void orderAndDisplayEvents() {
        addIconsToButtons();
        switch (orderByChoiceBox.getSelectionModel().getSelectedIndex()) {
            case 0: // Order by creation date
                allEvents.sort(Comparator.comparing(Event::getCreationDate).reversed());
                break;
            case 1: // Order by event name
                allEvents.sort(Comparator.comparing(o -> o.getTitle().toLowerCase()));
                break;
            case 2: // order by num of participants
                allEvents.sort(Comparator.comparingInt(o -> -o.getParticipants().size()));
                break;
            case 3: // order by last activity
                allEvents.sort(Comparator.comparing(Event::getLastActivity).reversed());
                break;
        }

        if (reverseOrderCheckBox.isSelected()) allEvents = allEvents.reversed();


        eventList.getChildren().clear();

        for (Event event : allEvents) {
            final EventListItemAdmin item = new EventListItemAdmin(
                    event.getTitle(),
                    event.getId(),
                    () -> deleteEventButton(event),
                    () -> eventExportHandler(event),
                    () -> {
                        stopPoller();
                        mainCtrl.showEventPage(event);
                    }
                    );
            eventList.getChildren().add(item);
        }
    }

    private void addIconsToButtons() {
//        String saveText = saveButton.getText();
//        if (!saveText.startsWith("\uD83D\uDDAB")) {
//            saveButton.setText("\uD83D\uDDAB " + saveText);
//        }

        String backBText = backButton.getText();
        if (!backBText.startsWith("\u2190")) {
            backButton.setText("\u2190 " + backBText);
        }
    }

    /**
     * Deletes the event from the server and refreshes the list
     *
     * @param event event to delete
     */
    private void deleteEventButton(Event event) {
        Confirmation confirmation =
                new Confirmation(String.format
                        (languageConf.get("AdminOverview.deleteEventConfirmationMessage"),
                                event.getId()),
                        languageConf.get("Confirmation.confirmation"),
                        languageConf);
        Optional<ButtonType> button = confirmation.showAndWait();
        if(button.isEmpty() || button.get().equals(ButtonType.CANCEL))
            return;
        int status;
        try {
            status = server.deleteEvent(event.getId());
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        if(status != 204) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("AdminOverview.deleteEventError"));
            alert.setHeaderText(String
                    .format(languageConf
                                    .get("AdminOverview.unexpectedServerResponse"),
                            status));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
        } else {
            allEvents.remove(event);
            loadAllEvents();
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
        stopPoller();
        mainCtrl.showAdminLogin();
    }

    /**
     * Reload the events with events from the server
     */
    public void loadAllEvents() {
        try {
            allEvents = server.getEvents(password);
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        orderAndDisplayEvents();
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
        if (initialDirectory != null && initialDirectory.exists())
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
        if (file == null) {
            // no file selected
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
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("AdminOverview.writeError"));
            alert.setHeaderText(languageConf.get("AdminOverview.exportError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
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
            // no file selected
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
                int status = server.importEvent(password, event);
                switch (status) {
                    case 400 -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                languageConf.get("AdminOverview.missingParticipantError"));
                        alert.setHeaderText(languageConf.get("AdminOverview.importError"));
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        alert.showAndWait();
                    }
                    case 409 -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                languageConf.get("AdminOverview.eventConflictError"));
                        alert.setHeaderText(languageConf.get("AdminOverview.importError"));
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        alert.showAndWait();
                    }
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        languageConf.get("AdminOverview.readError"));
                alert.setHeaderText(languageConf.get("AdminOverview.importError"));
                java.awt.Toolkit.getDefaultToolkit().beep();
                alert.showAndWait();
            }
        }
        loadAllEvents();
    }

    /**
     * Initialize the long poller
     * @param timeOut time in ms until server sends a time-out signal
     */
    public void initPoller(Long timeOut) {
        if(poller != null && poller.isAlive()) {
            return;
        }
        poller = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                int status;
                try {
                    status = server.pollEvents(password, timeOut);
                } catch (ConnectException e) {
                    Platform.runLater(mainCtrl::handleServerNotFound);
                    return;
                }
                if(status == 204)
                    Platform.runLater(this::loadAllEvents);
                else if(status != 408) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(languageConf.get("unexpectedError"));
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        alert.show();
                        stopPoller();
                        mainCtrl.showAdminLogin();
                    });
                    return;
                }
            }});
        poller.start();
    }

    /**
     * Stop the long poller
     */
    public void stopPoller() {
        if(poller != null && !poller.isInterrupted())
            poller.interrupt();
    }
}
