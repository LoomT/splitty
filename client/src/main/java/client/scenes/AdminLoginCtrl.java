package client.scenes;

import client.utils.ServerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import static client.AdminClient.*;

public class AdminLoginCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField usernameTextField;

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

    public AdminLoginCtrl(ServerUtils server, MainCtrl mainCtrl) {

        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Initialize method for adminLoginCtrl
     *
     */


    @FXML
    private void initialize() {
        setupListeners();
    }

    /**
     * Sets up the listeners for the buttons
     *
     */

    private void setupListeners() {
        loginButton.setOnAction(event -> login());
    }

    /**
     * To be implemented
     * would log in the admin to a page where he can access admin functions
     *
     */

    private void login() {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        System.out.println(verifyPassword(password));



    }
}
