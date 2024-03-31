package client.scenes;

import client.components.EventListItem;
import client.components.FlagListCell;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;


public class StartScreenCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
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
    private Button eventButton;
    @FXML
    private Button joinButton;
    @FXML
    private Button adminButton;


    private UserConfig userConfig;
    private Websocket websocket;

    /**
     * start screen controller constructor
     *
     * @param server       utils
     * @param mainCtrl     main scene controller
     * @param languageConf language config instance
     * @param userConfig   the user configuration
     * @param websocket the ws instance
     */
    @Inject
    public StartScreenCtrl(
            ServerUtils server,
            MainCtrl mainCtrl,
            LanguageConf languageConf,
            UserConfig userConfig,
            Websocket websocket
    ) {
        this.mainCtrl = mainCtrl;
        this.server = server;

        this.languageConf = languageConf;
        this.userConfig = userConfig;
        this.websocket = websocket;

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
        languageChoiceBox.setOnAction(event -> {
            languageConf.changeCurrentLocaleTo(languageChoiceBox.getValue());
        });
        reloadEventCodes();
    }

    /**
     * Reloads the event codes from the user config and updates the event list
     *
     */
    private void reloadEventCodes() {
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
                        () -> {
                            eventList.getChildren().remove(
                                    list.get(
                                            recentEventCodes.indexOf(eventCode)
                                    )
                            );
                        },
                        (String c) -> {
                            code.setText(c);
                            join();
                        }
                );
                list.add(eventListItem);
                eventList.getChildren().add(eventListItem);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
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
        reloadEventCodes();
    }


    /**
     * Creates and joins the event with provided title
     */
    public void create() {
        websocket.resetAllActions();
        if (title.getText().isEmpty()) return;
        try {
            Event createdEvent = server.createEvent(new Event(title.getText()));
            mainCtrl.showEventPage(createdEvent);

        } catch (WebApplicationException e) {
            System.out.println("Something went wrong while creating an event");
        }
    }

    /**
     * Tries to join the inputted event
     */
    public void join() {
        websocket.resetAllActions();
        if (code.getText().isEmpty()) return;
        try {
            Event joinedEvent = server.getEvent(code.getText());
            if(joinedEvent == null) {
                System.out.println("Event not found");
                // Show visually that event was not found
                // a full error pop up might be too annoying in this case
                return;
            }
            mainCtrl.showEventPage(joinedEvent);
        } catch (Exception e) {
            throw e;
            //System.out.println("Something went wrong while joining an event");
        }


    }

    public void checkEnter(Scene scene){
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
                System.out.println("Key Pressed: " + ke.getCode());
                System.out.println(ke.getTarget());
                if(ke.getTarget().equals(title) || ke.getTarget().equals(eventButton)){
                    create();
                }
                else if(ke.getTarget().equals(code) || ke.getTarget().equals(joinButton)){
                    join();
                }
                else if(ke.getTarget().equals(adminButton)) showAdminLogin();
                else return;
                ke.consume(); // <-- stops passing the event to next node
            }
        });
    }


    /**
     * Display admin login
     */
    public void showAdminLogin() {
        mainCtrl.showAdminLogin();
    }
}
