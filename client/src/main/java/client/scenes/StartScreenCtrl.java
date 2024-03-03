package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class StartScreenCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField title;

    @FXML
    private TextField code;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    /**
     * start screen controller constructor
     *
     * @param server utils
     * @param mainCtrl main scene controller
     */
    @Inject
    public StartScreenCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    /**
     * Initialize method for startscreenctrl
     */
    @FXML
    private void initialize() {
        languageChoiceBox.setValue(LanguageConf.getCurrentLocaleString());
        languageChoiceBox.getItems().addAll(LanguageConf.getAvailableLocalesString());
        languageChoiceBox.setOnAction(event -> {
            LanguageConf.changeCurrentLocaleTo(languageChoiceBox.getValue());
        });
    }


    /**
     * Creates and joins the event with provided title
     */
    public void create() {
        if(title.getText().isEmpty()) {
            // inform that title is empty
        }
        try {
            // addEvent should return the code
            //mainCtrl.showEvent(server.addEvent(title.getText()));
        } catch (WebApplicationException e) {
            //error
        }
    }

    /**
     * Tries to join the inputted event
     */
    public void join() {
//        if(server.existsEvent(code.getText())) {
//            mainCtrl.showEvent(code.getText());
//        } else {
//
//        }
    }

    /**
     * shows AdminLogin
     *
     * @param actionEvent action event
     */

    public void showAdminLogin(ActionEvent actionEvent) {
        mainCtrl.showAdminLogin();
    }
}
