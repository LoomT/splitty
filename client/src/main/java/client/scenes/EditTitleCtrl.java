package client.scenes;


import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.ConnectException;

import static commons.WebsocketActions.TITLE_CHANGE;

public class EditTitleCtrl {

    @FXML
    private TextField nameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Text eventTitle;

    @FXML
    private Text titleError;

    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final Websocket websocket;
    private final LanguageConf languageConf;

    private Event event;

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
        eventTitleListener(nameTextField, titleError, languageConf);
        websocket.on(TITLE_CHANGE, title -> {
            if(event != null)
                event.setTitle((String) title);
            eventTitle.setText((String) title);
        });
    }

    /**
     * Adds a listener to the titleField which will make the errorField visible
     * with a message informing the user that the
     * length of the text reached maxLength which is currently 50
     *
     * @param titleField event title text field
     * @param errorField error text node
     * @param languageConf languageConf
     */
    static void eventTitleListener(TextField titleField, Text errorField, LanguageConf languageConf) {
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorField.setVisible(false);
            int maxLength = 50;
            if(newValue.length() > maxLength) {
                newValue = newValue.substring(0, maxLength);
            }
            if(newValue.length() == maxLength) {
                errorField.setText(
                        String.format(languageConf.get("StartScreen.maxEventNameLength"), maxLength));
                errorField.setVisible(true);
            }
            titleField.setText(newValue);
        });
    }

    /**
     * Change the title of the EditTitle page
     * @param title new title
     */
    public void changeTitle(String title) {
        eventTitle.setText(title);
    }

    /**
     * Closes the popup.
     */
    @FXML
    public void cancelTitle(){
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Saves the new title if it isn't empty or over 100 characters.
     */
    @FXML
    public void saveTitle(){
        if(nameTextField.getText().isEmpty()) {
            titleError.setText(languageConf.get("StartScreen.emptyEventName"));
            titleError.setVisible(true);
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
        titleError.setVisible(false);
        eventTitle.setText(event.getTitle());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(languageConf.get("TitleChanger.pageTitle"));
        nameTextField.textProperty().setValue("");
        stage.show();
    }
}
