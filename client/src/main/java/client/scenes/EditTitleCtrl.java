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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.http.ResponseEntity;

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

    public void initialize(){
        wordLimitError(nameTextField, titleError, 100);
    }

    @FXML
    public void cancelTitle(){
        nameTextField.textProperty().setValue("");
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

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

    public void setEventPageCtrl(EventPageCtrl eventPage){
        this.eventPageCtrl = eventPage;
    }

    /**
     *
     * @param textField
     * @param errorMessage
     * @param limit
     */
    public void wordLimitError(TextField textField, Text errorMessage, int limit){
        String message = errorMessage.getText();
        errorMessage.setVisible(false);
        textField.textProperty().addListener((observableValue, number, t1)->{
            errorMessage.setVisible(true);
            errorMessage.textProperty().bind(Bindings.concat(
                    message, String.format(" %d/%d", textField.getText().length(), limit)));

            errorMessage.setVisible(textField.getLength() > limit);
        });
    }
}
