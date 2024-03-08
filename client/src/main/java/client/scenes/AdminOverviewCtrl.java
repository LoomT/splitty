package client.scenes;

import client.components.EventListItem;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class AdminOverviewCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private UserConfig userConfig;


    @FXML
    private VBox eventList;


    /**
     * adminOverview screen controller constructor
     *
     * @param server   utils
     * @param mainCtrl main scene controller
     * @param userConfig the user configuration
     */
    @Inject
    public AdminOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, UserConfig userConfig) {

        this.server = server;
        this.mainCtrl = mainCtrl;
        this.userConfig = userConfig;

    }


    /**
     * This method is called when the fxml is loaded
     *
     */
    @FXML
    private void initialize() {
        reloadEventCodes();
    }


    @FXML
    private void backButtonClicked() {
        mainCtrl.showStartScreen();
    }

    /**
     * This method fetches the event codes and updates the list
     */
    private void reloadEventCodes() {
        List<String> recentEventCodes = userConfig.getRecentEventCodes();
        List<EventListItem> list = new ArrayList<>();
        eventList.getChildren().clear();


        for (int i = 0; i < recentEventCodes.size(); i++) {
            int finalI = i;
            list.add(
                    new EventListItem(
                            recentEventCodes.get(i),
                            () -> {
                                eventList.getChildren().remove(list.get(finalI));
                            },
                            (String c) -> {

                            }));
            eventList.getChildren().add(list.get(i));

        }
    }


}
