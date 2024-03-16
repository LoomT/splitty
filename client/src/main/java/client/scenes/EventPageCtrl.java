package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;

import javafx.scene.text.Text;


public class EventPageCtrl {

    @FXML
    private Text eventTitle;

    @FXML
    private Text participantText;

    @FXML
    private Tab allTab;

    @FXML
    private Tab fromTab;

    @FXML
    private Tab includingTab;

    @FXML
    private ChoiceBox<String> participantChoiceBox;
    @FXML
    private Button addExpenseButton;


    private int selectedParticipantId;


    private ServerUtils server;
    private MainCtrl mainCtrl;
    private LanguageConf languageConf;
    private Event event;

    /**
     * @param server   server utils injection
     * @param mainCtrl mainCtrl injection
     * @param languageConf the language config instance
     */
    @Inject
    public EventPageCtrl(ServerUtils server, MainCtrl mainCtrl, LanguageConf languageConf) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;

    }

    /**
     * call this function to set all the text on the eventpage to a given event
     *
     * @param e the event to be shown
     */
    public void displayEvent(Event e) {
        this.event = e;
        eventTitle.setText(e.getTitle());
        participantChoiceBox.getItems().clear();
        participantChoiceBox.setValue("");
        if (e.getParticipants().isEmpty()) {
            participantText.setText(languageConf.get("EventPage.noParticipantsYet"));
            allTab.setStyle("-fx-opacity:0");
            allTab.setDisable(true);
            fromTab.setStyle("-fx-opacity:0");
            fromTab.setDisable(true);
            includingTab.setStyle("-fx-opacity:0");
            includingTab.setDisable(true);
            addExpenseButton.setDisable(true);
        } else {
            allTab.setStyle("-fx-opacity:1");
            allTab.setDisable(false);
            fromTab.setStyle("-fx-opacity:1");
            fromTab.setDisable(false);
            includingTab.setStyle("-fx-opacity:1");
            includingTab.setDisable(false);
            addExpenseButton.setDisable(false);
            StringBuilder p = new StringBuilder();
            for (int i = 0; i < e.getParticipants().size(); i++) {
                p.append(e.getParticipants().get(i).getName());
                if (i != e.getParticipants().size() - 1) p.append(", ");
            }
            participantText.setText(p.toString());


            participantChoiceBox.getItems().addAll(
                    e.getParticipants().stream().map(Participant::getName).toList()
            );
            participantChoiceBox.setValue(e.getParticipants().get(0).getName());
            selectedParticipantId = 0;

            String name = e.getParticipants().get(selectedParticipantId).getName();
            fromTab.setText(languageConf.get("EventPage.from") + " " + name);
            includingTab.setText(languageConf.get("EventPage.including") + " " + name);
        }

        participantChoiceBox.setOnAction(event -> {
            selectedParticipantId = participantChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedParticipantId < 0) return;

            String name = e.getParticipants().get(selectedParticipantId).getName();
            fromTab.setText(languageConf.get("EventPage.from") + " " + name);
            includingTab.setText(languageConf.get("EventPage.including") + " " + name);
        });
    }

    @FXML
    private void backButtonClicked() {
        mainCtrl.showStartScreen();
    }

    @FXML
    private void tabSelectionChanged() {

    }


    @FXML
    private void sendInvitesClicked() {

    }

    @FXML
    private void editParticipantsClicked() {
        mainCtrl.showEditParticipantsPage(event);
    }

    @FXML
    private void addExpenseClicked() {

    }


}
