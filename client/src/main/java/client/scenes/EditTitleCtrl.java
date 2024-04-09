package client.scenes;


import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.ConnectException;

import static client.utils.CommonFunctions.lengthListener;
import static commons.WebsocketActions.TITLE_CHANGE;

public class EditTitleCtrl {

    @FXML
    private TextField nameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Text eventTitle;

    @FXML
    private Label warningLabel;

    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final Websocket websocket;
    private final LanguageConf languageConf;

    private Event event;
    private Stage stage;

    /**
     * start screen controller constructor
     *
     * @param mainCtrl main controller
     * @param server server utils
     * @param websocket websocket client
     * @param languageConf language config instance
     */
    @Inject
    public EditTitleCtrl(MainCtrlInterface mainCtrl, ServerUtils server, Websocket websocket,
            LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.websocket = websocket;
        this.languageConf = languageConf;
    }

    /**
     * Initializes the characterLimitError event listener.
     */
    public void initialize(){
        lengthListener(nameTextField, warningLabel, 30,
                languageConf.get("StartScreen.maxEventNameLength"));
        websocket.on(TITLE_CHANGE, title -> {
            if(event != null)
                event.setTitle((String) title);
            eventTitle.setText((String) title);
        });
    }

    /**
     * Closes the popup.
     */
    @FXML
    public void cancelTitle(){
        stage.close();
    }

    /**
     * Saves the new title if it isn't empty or over 100 characters.
     */
    @FXML
    public void saveTitle(){
        if(nameTextField.getText().isEmpty()) {
            warningLabel.setText(languageConf.get("StartScreen.emptyEventName"));
            warningLabel.setVisible(true);
            return;
        }

        event.setTitle(nameTextField.getText());
        int result;
        try {
            result = server.updateEventTitle(event);
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        if(result >= 400)
            System.out.println("An error has occurred");
        else{
            System.out.println("Event Title changed to: " + nameTextField.getText());
            cancelTitle();
        }
    }

    /**
     * Sets up the EditTitle screen and displays it.
     * @param event Event which is displayed
     * @param stage Stage at which the editEventTitle will be displayed
     */
    public void displayEditEventTitle(Event event, Stage stage){
        this.event = event;
        this.stage = stage;
        warningLabel.setVisible(false);
        eventTitle.setText(event.getTitle());
        nameTextField.textProperty().setValue("");
    }
}
