package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import commons.WebsocketActions;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;


public class EditParticipantsCtrl {
    @FXML
    private Text eventTitle;
    @FXML
    private ChoiceBox<String> chooseParticipant;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ibanField;
    @FXML
    private TextField bicField;
    @FXML
    private Button saveButton;

    private Event event;
    private ServerUtils server;
    private MainCtrl mainCtrl;
    private LanguageConf languageConf;
    private Websocket websocket;
    private String previousEventId = "";

    /**
     * @param server       serverutils instance
     * @param mainCtrl     main control instance
     * @param languageConf the language config instance
     * @param websocket    the websocket instance
     */
    @Inject
    public EditParticipantsCtrl(
            ServerUtils server,
            MainCtrl mainCtrl,
            LanguageConf languageConf,
            Websocket websocket
    ) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.websocket = websocket;


    }

    /**
     * Call this function when showing this page
     *
     * @param e the event to edit the participants for
     */
    public void displayEditParticipantsPage(Event e) {
        this.event = e;
        eventTitle.setText(e.getTitle());

        resetFields();

        chooseParticipant.getItems().clear();

        chooseParticipant.getItems().add(languageConf.get("EditP.newParticipant"));
        chooseParticipant
            .getItems()
            .addAll(
                e.getParticipants()
                    .stream()
                    .map(Participant::getName)
                    .toList()
            );

        chooseParticipant.setValue(languageConf.get("EditP.newParticipant"));


        chooseParticipant.setOnAction((event1) -> {
            int index = chooseParticipant.getSelectionModel().getSelectedIndex();
            if (index < 0) return;
            if (index == 0) {
                resetFields();
            } else {
                saveButton.setText(languageConf.get("EditP.save"));
                Participant p = event.getParticipants().get(index - 1);
                nameField.setText(p.getName());
                emailField.setText(p.getEmailAddress());

            }
        });

    }

    private void registerParticipantChangeListener() {
        if (previousEventId.equals(event.getId())) return;
        previousEventId = event.getId();
        websocket.resetAction(WebsocketActions.UPDATE_PARTICIPANT);
        websocket.resetAction(WebsocketActions.ADD_PARTICIPANT);
        websocket.resetAction(WebsocketActions.REMOVE_PARTICIPANT);

        websocket.on(WebsocketActions.UPDATE_PARTICIPANT, (Object part) -> {
            Participant p = (Participant) part;
            int index = -1;
            for (int i = 0; i < event.getParticipants().size(); i++) {
                Participant curr = event.getParticipants().get(i);
                if (curr.getParticipantId() == p.getParticipantId()) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The updated participant's ID ("
                        + p.getParticipantId() +
                        ") does not match with any ID's of the already existing participants");
            }
            event.getParticipants().remove(index);
            event.getParticipants().add(index, p);
            displayEditParticipantsPage(event);
        });
        websocket.on(WebsocketActions.ADD_PARTICIPANT, (Object part) -> {
            Participant p = (Participant) part;
            event.getParticipants().add(p);
            displayEditParticipantsPage(event);
        });
        websocket.on(WebsocketActions.REMOVE_PARTICIPANT, (Object part) -> {
            long partId = (long) part;
            int index = -1;
            for (int i = 0; i < event.getParticipants().size(); i++) {
                Participant curr = event.getParticipants().get(i);
                if (curr.getParticipantId() == partId) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The deleted participant's ID ("
                        + partId +
                        ") does not match with any ID's of the already existing participants");
            }
            event.getParticipants().remove(index);
            displayEditParticipantsPage(event);
        });
    }

    private void resetFields() {
        saveButton.setText(languageConf.get("EditP.createParticipant"));
        nameField.setText("");
        emailField.setText("");
        ibanField.setText("");
        bicField.setText("");

    }

    /**
     * Handler for the back button
     */
    @FXML
    private void backButtonClicked() {
        mainCtrl.showEventPage(event);
    }

    /**
     * Handler for the save button
     */
    @FXML
    private void saveButtonClicked() {
        int index = chooseParticipant.getSelectionModel().getSelectedIndex();
        System.out.println("Creating/saving participant " + index);

        String name = nameField.getText();
        String email = emailField.getText();

        if (index < 0) return;
        if (index == 0) {
            // create a new participant
            Participant newP = new Participant(name, email);
            server.createParticipant(event.getId(), newP);
        } else {
            Participant currP = event.getParticipants().get(index - 1);
            currP.setName(name);
            currP.setEmailAddress(email);
            server.updateParticipant(event.getId(), currP);
        }

    }
}
