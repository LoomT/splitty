package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;

public class AdminOverviewCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;


    /**
     * adminOverview screen controller constructor
     *
     * @param server   utils
     * @param mainCtrl main scene controller
     */
    @Inject
    public AdminOverviewCtrl(ServerUtils server, MainCtrl mainCtrl) {

        this.server = server;
        this.mainCtrl = mainCtrl;
    }


    @FXML
    private void backButtonClicked() {
        mainCtrl.showStartScreen();
    }


}
