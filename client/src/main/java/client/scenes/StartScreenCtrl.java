package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.EventListItem;
import client.components.FlagListCell;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import static client.utils.CommonFunctions.lengthListener;


public class StartScreenCtrl {

    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;

    @FXML
    private TextField title;

    @FXML
    private TextField code;

    @FXML
    private ComboBox<String> languageChoiceBox;

    @FXML
    private VBox eventList;

    @FXML
    private Label joinError;

    @FXML
    private Label createEventError;

    private final UserConfig userConfig;

    /**
     * start screen controller constructor
     *
     * @param server       utils
     * @param mainCtrl     main scene controller
     * @param languageConf language config instance
     * @param userConfig   the user configuration
     */
    @Inject
    public StartScreenCtrl(
            ServerUtils server,
            MainCtrlInterface mainCtrl,
            LanguageConf languageConf,
            UserConfig userConfig
    ) {
        this.mainCtrl = mainCtrl;
        this.server = server;

        this.languageConf = languageConf;
        this.userConfig = userConfig;
    }

    /**
     * Initialize method for startscreenctrl
     */
    @FXML
    private void initialize() {
        languageChoiceBox.setValue(languageConf.getCurrentLocaleString());
        languageChoiceBox.getItems().addAll(languageConf.getAvailableLocalesString());
        languageChoiceBox.setButtonCell(new FlagListCell(languageConf));
        languageChoiceBox.setCellFactory(param -> new FlagListCell(languageConf));
        languageChoiceBox.setOnAction(event ->
                languageConf.changeCurrentLocaleTo(languageChoiceBox.getValue()));
        joinError.setVisible(false);
        createEventError.setVisible(false);
        code.textProperty().addListener((observable, oldValue, newValue) -> {
            joinError.setVisible(false);
            String filteredValue = newValue.replaceAll("[^a-zA-Z]", "");
            if(filteredValue.length() > 5) filteredValue = filteredValue.substring(0, 5);
            code.setText(filteredValue.toUpperCase());
        });
        lengthListener(title, createEventError, 30,
                languageConf.get("StartScreen.maxEventNameLength"));

    }

    /**
     * Reloads the event codes from the user config and updates the event list
     *
     */
    public void reloadEventCodes() {
        List<String> recentEventCodes = userConfig.getRecentEventCodes();
        List<EventListItem> list = new ArrayList<>();

        eventList.getChildren().clear();

        for (String eventCode : recentEventCodes) {
            try {
                Event event = server.getEvent(eventCode);
                if (event == null) {
                    throw new IllegalArgumentException("Event does not exist for code: "
                            + eventCode);
                }
                EventListItem eventListItem = new EventListItem(
                        event.getTitle(),
                        eventCode,
                        () -> eventList.getChildren().remove(
                                list.get(
                                        recentEventCodes.indexOf(eventCode)
                                )
                        ),
                        code::setText
                );
                list.add(eventListItem);
                eventList.getChildren().add(eventListItem);
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
                break;
            }
        }
    }


    /**
     * Call this when you want to load/reload the start screen,
     * for example when you exit the event page with the back button to reset the fields.
     */
    public void reset() {
        title.setText("");
        code.setText("");
        joinError.setVisible(false);
        createEventError.setVisible(false);
    }

    /**
     * Creates and joins the event with provided title
     */
    public void create() {
        if (title.getText().isEmpty()){
            createEventError.setText(languageConf.get("StartScreen.emptyEventName"));
            createEventError.setVisible(true);
            return;
        }
        try {
            Event createdEvent = server.createEvent(new Event(title.getText()));
            mainCtrl.showEventPage(createdEvent);
        } catch (WebApplicationException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("restartTheAppMessage"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
        } catch (ConnectException e) {
            showServerNotFoundError();
        }
    }


    /**
     * Tries to join the inputted event
     */
    public void join() {
        if(code.getText().isEmpty() || code.getText().length() != 5){
            joinError.setText(languageConf.get("StartScreen.invalidJoinCode"));
            joinError.setVisible(true);
            return;
        }
        try {
            Event joinedEvent = server.getEvent(code.getText());
            if(joinedEvent == null) {
                joinError.setText(languageConf.get("StartScreen.eventNotFoundMessage"));
                joinError.setVisible(true);
                return;
            }
            mainCtrl.showEventPage(joinedEvent);
        } catch (ConnectException e) {
            showServerNotFoundError();
        }
    }


    /**
     * Display admin login
     */
    public void showAdminLogin() {
        mainCtrl.showAdminLogin();
    }

    /**
     * Shows the error if the server is unreachable for some reason
     */
    public void showServerNotFoundError() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                languageConf.get("StartScreen.serverUnavailableErrorMessage"));
        alert.setHeaderText(languageConf.get("StartScreen.serverUnavailableErrorHeader"));
        alert.show();
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Open options when options button is clicked
     */
    @FXML
    public void optionsClicked() {
        mainCtrl.openOptions();
    }
}
