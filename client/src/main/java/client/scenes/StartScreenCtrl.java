package client.scenes;

import client.components.EventListItem;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;


import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


import java.util.ArrayList;
import java.util.List;


public class StartScreenCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageConf languageConf;

    @FXML
    private TextField title;

    @FXML
    private TextField code;

    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private VBox eventList;

    /**
     * start screen controller constructor
     *
     * @param server   utils
     * @param mainCtrl main scene controller
     * @param languageConf language config instance
     */
    @Inject
    public StartScreenCtrl(ServerUtils server, MainCtrl mainCtrl, LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.server = server;

        this.languageConf = languageConf;

    }

    /**
     * Initialize method for startscreenctrl
     */
    @FXML
    private void initialize() {
        languageChoiceBox.setValue(languageConf.getCurrentLocaleString());
        languageChoiceBox.getItems().addAll(languageConf.getAvailableLocalesString());
        languageChoiceBox.setOnAction(event -> {
            languageConf.changeCurrentLocaleTo(languageChoiceBox.getValue());
        });

        List<String> testList = List.of("Test1", "random event",
                "heres one more", "idk", "try deleting this");
        List<EventListItem> list = new ArrayList<>();


        for (int i = 0; i < testList.size(); i++) {
            int finalI = i;
            list.add(new EventListItem(testList.get(i), () -> {
                eventList.getChildren().remove(list.get(finalI));
            }));
            eventList.getChildren().add(list.get(i));

        }
    }


    /**
     * Creates and joins the event with provided title
     */
    public void create() {
        if (title.getText().isEmpty()) {
            // inform that title is empty
        }
        try {
            // addEvent should return the code
            //mainCtrl.showEvent(server.addEvent(title.getText()));
        } catch (WebApplicationException e) {
            //error
        }
    }


    /**
     * Tries to join the inputted event
     */
    public void join() {
        if (code.getText().isEmpty()) return;
        try {
            Event joinedEvent = server.getEvent(code.getText());
            mainCtrl.showEventPage(joinedEvent);
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }


    }
}
