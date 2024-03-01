package client.scenes;

import client.components.EventListItem;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
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
     * @param server utils
     * @param mainCtrl main scene controller
     */
    @Inject
    public StartScreenCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    /**
     * Initialize method for startscreenctrl
     */
    @FXML
    private void initialize() {
        languageChoiceBox.setValue(LanguageConf.getCurrentLocaleString());
        languageChoiceBox.getItems().addAll(LanguageConf.getAvailableLocalesString());
        languageChoiceBox.setOnAction(event -> {
            LanguageConf.changeCurrentLocaleTo(languageChoiceBox.getValue());
        });

        List<String> testList = List.of("Test1", "random event", "heres one more", "idk", "try deleting this");
        List<EventListItem> list = new ArrayList<>();



        for (int i = 0; i < testList.size(); i++) {
            int finalI = i;
            list.add(new EventListItem(testList.get(i), ()->{
                eventList.getChildren().remove(list.get(finalI));
            }));
            eventList.getChildren().add(list.get(i));

        }
    }


    /**
     * Creates and joins the event with provided title
     */
    public void create() {
        if(title.getText().isEmpty()) {
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
//        if(server.existsEvent(code.getText())) {
//            mainCtrl.showEvent(code.getText());
//        } else {
//
//        }
        System.out.println("Clicked join");
        System.out.println(ServerUtils.getEvent(code.getText()));
    }
}
