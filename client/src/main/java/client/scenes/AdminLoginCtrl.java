package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class AdminLoginCtrl{

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    @FXML
    private Label passwordLabel;

    @FXML
    private TextField passwordTextField;

    /**
     * adminLogin screen controller constructor
     *
     * @param server utils
     * @param mainCtrl main scene controller
     */
    @Inject
    public AdminLoginCtrl(ServerUtils server, MainCtrl mainCtrl) {

        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Initializes the shortcuts for AdminLogin:
     *      Escape: go back
     *      Enter: on textField login
     * @param scene scene the listeners are initialised in
     */
    public void initializeShortcuts(Scene scene) {
        MainCtrl.checkKey(scene, this::backButtonClicked, KeyCode.ESCAPE);
        MainCtrl.checkKey(scene, this::loginButtonClicked,  passwordTextField, KeyCode.ENTER);
    }


    /**
     * Method to handle the back button click
     *
     */
    @FXML
    public void backButtonClicked() {
        mainCtrl.showStartScreen();
    }


    /**
     * Method to handle the login button click
     *
     */
    @FXML
    private void loginButtonClicked() {
        String password = passwordTextField.getText();
        if (server.verifyPassword(password)) {
            mainCtrl.showAdminOverview(password, 5000L);
        } else {
            passwordLabel.setText("Incorrect password");
        }
    }

}

