package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AdminLoginCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    @FXML
    private Label passwordLabel;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button loginButton;

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
        String password = passwordTextField.getText();
        if (server.verifyPassword(password)) {
            mainCtrl.showAdminOverview(password, 5000L);
        } else {
            passwordLabel.setText("Incorrect password");
        }
    }

}

