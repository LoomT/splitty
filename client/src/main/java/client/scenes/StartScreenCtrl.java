package client.scenes;

import client.components.EventListItem;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static client.scenes.ErrorPopupCtrl.ErrorCode.*;


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

    @FXML
    private Text joinError;

    @FXML
    private Text createEventError;

    private UserConfig userConfig;
    private Websocket websocket;

    /**
     * start screen controller constructor
     *
     * @param server       utils
     * @param mainCtrl     main scene controller
     * @param languageConf language config instance
     * @param userConfig   the user configuration
     * @param websocket the ws instance
     */
    @Inject
    public StartScreenCtrl(
            ServerUtils server,
            MainCtrl mainCtrl,
            LanguageConf languageConf,
            UserConfig userConfig,
            Websocket websocket
    ) {
        this.mainCtrl = mainCtrl;
        this.server = server;

        this.languageConf = languageConf;
        this.userConfig = userConfig;
        this.websocket = websocket;

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
        reloadEventCodes();
        wordLimitError(code, joinError, 5);
        wordLimitError(title, createEventError,100);

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
                                code.setText(c);
                            }));
            eventList.getChildren().add(list.get(i));

        }
    }

    /**
     * Call this when you want to load/reload the start screen,
     * for example when you exit the event page with the back button to reset the fields.
     */
    public void reset() {
        title.setText("");
        code.setText("");
        reloadEventCodes();
    }

    /**
     *
     * @param textField
     * @param errorMessage
     * @param limit
     */
    public void wordLimitError(TextField textField, Text errorMessage, int limit){
        String message = errorMessage.getText();
        errorMessage.setFill(Color.RED);
        errorMessage.setVisible(false);
        textField.textProperty().addListener((observableValue, number, t1)->{
            errorMessage.setVisible(true);
            errorMessage.textProperty().bind(Bindings.concat(
                    message, String.format(" %d/%d", textField.getText().length(), limit)));

            errorMessage.setVisible(textField.getLength() > limit);
        });
    }



    /**
     * Creates and joins the event with provided title
     */
    public void create() {
        websocket.resetAllActions();
        String token;
        if (title.getText().isEmpty()){
            System.out.println("Empty Title Error");
            token = "StartScreen.emptyEventToken";
            mainCtrl.showErrorPopup(EmptyStringError, token, 0);
            return;
        }
        else if(title.getText().length() > 100){
            System.out.println("Word Limit Error");
            token = "StartScreen.eventWordLimitToken";
            mainCtrl.showErrorPopup(WordLimitError, token ,100);
            return;
        }
        try {
            Event createdEvent = server.createEvent(new Event(title.getText()));
            mainCtrl.showEventPage(createdEvent);

        } catch (WebApplicationException e) {
            System.out.println("Something went wrong while creating an event");
        }
    }


    /**
     * Tries to join the inputted event
     */
    public void join() {
        websocket.resetAllActions();
        String token;
        if (code.getText().isEmpty()){
            token = "StartScreen.joinEmptyToken";
            System.out.println("Empty Field Error");
            mainCtrl.showErrorPopup(EmptyStringError, token, 0);
            return;
        }
        if(code.getText().length() > 5){
            token = "StartScreen.joinWordLimitToken";
            System.out.println("Word Limit Error");
            mainCtrl.showErrorPopup(WordLimitError, token, 5);
            return;
        }
        if(code.getText().length() != 5){
            token = "StartScreen.joinInvalidToken";
            System.out.println("Join Code Error");
            mainCtrl.showErrorPopup(InvalidErrorCode, token, 5);
            return;
        }
        try {
            Event joinedEvent = server.getEvent(code.getText());
            mainCtrl.showEventPage(joinedEvent);
        } catch (Exception e) {
            throw e;
        }


    }


    /**
     * Display admin login
     */
    public void showAdminLogin() {
        mainCtrl.showAdminLogin();
    }
}
