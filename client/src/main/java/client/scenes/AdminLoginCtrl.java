package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.ConnectException;

public class AdminLoginCtrl {

    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;
    @FXML
    private Label warningLabel;
    @FXML
    private TextField passwordTextField;

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
     * Runs when the app first starts
     */
    public void initialize() {
        warningLabel.setVisible(false);
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
    private void backButtonClicked() {
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

