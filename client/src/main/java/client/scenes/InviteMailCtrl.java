package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import com.google.inject.Inject;
import commons.Event;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class InviteMailCtrl {

    private Event event;
    private Stage stage;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final UserConfig userConfig;
    private FadeTransition ft;

    @FXML
    private TextField emailField;
    @FXML
    private ProgressIndicator loadIndicator;
    @FXML
    private Label confirmationLabel;


    @Inject
    public InviteMailCtrl(MainCtrl mainCtrl, LanguageConf languageConf, UserConfig userConfig){
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.userConfig = userConfig;
    }

    public void initialize(){
        ft = new FadeTransition(Duration.millis(2000), confirmationLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(1000));
        ft.setOnFinished(e -> confirmationLabel.setVisible(false));
        loadIndicator.setVisible(false);
    }

    public void ShowInviteMail(Event event, Stage stage){
        this.event = event;
        this.stage = stage;
    }

    private void checkClicked(){

    }
}
