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
    private Label usernameLabel;

    @FXML
    private TextField usernameTextField;

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

//    @FXML
//    private void initialize() {
//        setupListeners();
//    }

//    private void setupListeners() {
//        loginButton.setOnAction(event -> login());
//    }
//
//    private void login() {
//        String password = passwordTextField.getText();
//
//
//    }

    @FXML
    private void backButtonClicked() {
        mainCtrl.showStartScreen();
    }

    @FXML
    private void loginButtonClicked() {
        mainCtrl.showAdminOverview();
    }

}

