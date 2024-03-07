package client.scenes;

import client.Websocket;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXML;
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
    private int selectedParticipantId;

    private Websocket websocket;

    private ServerUtils server;
    private MainCtrl mainCtrl;
    private Event event;

    /**
     * @param server server utils injection
     * @param mainCtrl mainCtrl injection
     */
    @Inject
    public EventPageCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;

    }

    /**
     * call this function to set all the text on the eventpage to a given event
     * @param e the event to be shown
     */
    public void displayEvent(Event e) {
        this.event = e;
        System.out.println(e.getTitle());
        eventTitle.setText(e.getTitle());

        if (e.getParticipants().isEmpty()) {
            participantText.setText("No participants yet");
        } else {
            String p = "";
            for (int i = 0; i < e.getParticipants().size(); i++) {
                p += e.getParticipants().get(i).getName();
                if (i != e.getParticipants().size() - 1) p += ", ";
            }
            participantText.setText(p);
        }

        participantChoiceBox.getItems().addAll(
                e.getParticipants().stream().map(Participant::getName).toList()
        );
//        participantChoiceBox.setValue(e.getParticipants().get(0).getName());
//        selectedParticipantId = 0;

        participantChoiceBox.setOnAction(event -> {
            selectedParticipantId = participantChoiceBox.getSelectionModel().getSelectedIndex();
            String name = e.getParticipants().get(selectedParticipantId).getName();
            //fromTab.setText("From " + name);
            //includingTab.setText("Including " + name);
        });

        websocket = new Websocket(this);
        websocket.connect(e.getId());
    }

    /**
     * Changes the title of the event
     *
     * @param newTitle new title of the event
     */
    public void changeTitle(String newTitle) {
        event.setTitle(newTitle);
        eventTitle.setText(newTitle);
    }

    @FXML
    private void tabSelectionChanged() {

    }


    @FXML
    private void sendInvitesClicked() {

    }

    @FXML
    private void editParticipantsClicked() {

    }

    @FXML
    private void addExpenseClicked() {

    }


}
