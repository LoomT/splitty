package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.net.ConnectException;

public class AdminLoginCtrl {

    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;
    @FXML
    private Label warningLabel;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button backButton;

    /**
     * adminLogin screen controller constructor
     *
     * @param server utils
     * @param mainCtrl main scene controller
     */
    @Inject
    public AdminLoginCtrl(ServerUtils server, MainCtrlInterface mainCtrl) {
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

    private void addIconsToButtons() {
//        String saveText = saveButton.getText();
//        if (!saveText.startsWith("\uD83D\uDDAB")) {
//            saveButton.setText("\uD83D\uDDAB " + saveText);
//        }

        String backBText = backButton.getText();
        if (!backBText.startsWith("\u2190")) {
            backButton.setText("\u2190 " + backBText);
        }
    }

    /**
     * Runs when the app first starts
     */
    public void initialize() {
        warningLabel.setVisible(false);
        addIconsToButtons();
    }

    /**
     * resets the fields
     */
    public void display() {
        warningLabel.setVisible(false);
        passwordTextField.setText("");
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
        warningLabel.setVisible(false);
        if(passwordTextField.getText() == null || passwordTextField.getText().isEmpty()) {
            return;
        }
        String password = passwordTextField.getText();
        try {
            if (server.verifyPassword(password)) {
                mainCtrl.showAdminOverview(password, 5000L);
            } else {
                warningLabel.setVisible(true);
            }
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
        }
    }

}

