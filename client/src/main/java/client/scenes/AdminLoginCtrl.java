package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AdminLoginCtrl {

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
            mainCtrl.showAdminOverview(password);
        } else {
            passwordLabel.setText("Incorrect password");
        }
    }

    public void checkEscape(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
                System.out.println("Key Pressed: " + ke.getCode());
                backButtonClicked();
                ke.consume(); // <-- stops passing the event to next node
            } else if (ke.getCode() == KeyCode.ENTER  && ke.getTarget().equals(passwordTextField)) {
                loginButtonClicked();
            }
        });
    }
}

