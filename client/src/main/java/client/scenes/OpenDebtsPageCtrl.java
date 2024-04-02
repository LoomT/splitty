package client.scenes;

import client.utils.ServerUtils;
import commons.Event;
import jakarta.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class OpenDebtsPageCtrl {

    @FXML
    private Button backButton;

    private Event event;

    private ServerUtils server;

    private MainCtrl mainCtrl;


    /**
     * Constructor
     *
     * @param serverUtils the server utils
     * @param mainCtrl the main controller
     */
    @Inject
    public OpenDebtsPageCtrl(
            ServerUtils serverUtils,
            MainCtrl mainCtrl
    ) {
        this.server = serverUtils;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Displays the open debts page
     *
     * @param event the event
     */
    public void displayOpenDebtsPage(Event event) {
        this.event = event;
    }


    /**
     * Handles the back button click event functionality
     */
    @FXML
    public void backButtonClicked() {
        mainCtrl.goBackToEventPage(event);
    }
}
