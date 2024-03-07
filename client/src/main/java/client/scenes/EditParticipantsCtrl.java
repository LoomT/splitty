package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXML;
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

    private Event event;
    private ServerUtils server;
    private MainCtrl mainCtrl;

    /**
     * @param server   serverutils instance
     * @param mainCtrl main control instance
     */
    @Inject
    public EditParticipantsCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;

    }

    /**
     * Call this function when showing this page
     *
     * @param e the event to edit the participants for
     */
    public void displayEditParticipantsPage(Event e) {
        this.event = e;
        eventTitle.setText(e.getTitle());

        chooseParticipant.getItems().add("New Participant");
        chooseParticipant
            .getItems()
            .addAll(
                e.getParticipants()
                    .stream()
                    .map(Participant::getName)
                    .toList()
            );

        chooseParticipant.setValue("New Participant");
        chooseParticipant.setOnAction((event1) -> {
            int index = chooseParticipant.getSelectionModel().getSelectedIndex();
            if (index == 0) {
                nameField.setText("");
                emailField.setText("");
                ibanField.setText("");
                bicField.setText("");
            } else {
                Participant p = event.getParticipants().get(index - 1);
                nameField.setText(p.getName());
                emailField.setText(p.getEmailAddress());

            }
        });

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

    }
}
