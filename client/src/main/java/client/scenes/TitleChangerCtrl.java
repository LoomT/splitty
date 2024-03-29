package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TitleChangerCtrl {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageConf languageConf;

    @FXML
    private TextField nameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

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
    public TitleChangerCtrl(
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
    @FXML
    public void cancelTitle(){
        nameTextField.textProperty().setValue("");
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void saveTitle(){
        if(eventPageCtrl == null) return;
        eventPageCtrl.changeTitle(nameTextField.getText());
        cancelTitle();
    }

    public void setEventPageCtrl(EventPageCtrl eventPage){
        this.eventPageCtrl = eventPage;
    }
}
