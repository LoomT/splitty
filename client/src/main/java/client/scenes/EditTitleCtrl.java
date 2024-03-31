package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EditTitleCtrl {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageConf languageConf;

    @FXML
    private TextField nameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;
    @FXML
    private Text titleError;

    private EventPageCtrl eventPageCtrl;

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
    public EditTitleCtrl(
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
     * Initializes the characterLimitError event listener.
     */
    public void initialize(){
        characterLimitError(nameTextField, titleError, 100);
    }

    /**
     * Closes the popup and erases the textField
     */
    @FXML
    public void cancelTitle(){
        nameTextField.textProperty().setValue("");
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Saves the new title if it isn't empty or over 100 characters.
     */
    @FXML
    public void saveTitle(){
        if(eventPageCtrl == null
                || nameTextField.getText().isEmpty()
                || nameTextField.getLength() > 100) return;

        int result = eventPageCtrl.changeTitle(nameTextField.getText());
        if(result >= 400)
            System.out.println("An error has occurred");
        else{
            System.out.println("An error has occurred");
            cancelTitle();
        }
    }

    /**
     *
     * @param eventPage currentEventPageCtrl to edit the Event.
     */
    public void setEventPageCtrl(EventPageCtrl eventPage){
        this.eventPageCtrl = eventPage;
    }

    /**
     * Creates a characterLimitError which showcases an error
     * iff a character limit has been exceeded.
     * @param textField textField which is observed
     * @param errorMessage Text where the message is displayed in the scene
     * @param limit character limit to not be exceeded
     */
    public void characterLimitError(TextField textField, Text errorMessage, int limit){
        String message = errorMessage.getText();
        errorMessage.setVisible(false);
        textField.textProperty().addListener((observableValue, number, t1)->{
            errorMessage.textProperty().bind(Bindings.concat(
                    message, String.format(" %d/%d", textField.getText().length(), limit)));

            errorMessage.setVisible(textField.getLength() > limit);
        });
    }
}
