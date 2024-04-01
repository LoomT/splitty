package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
    private TextField beneficiaryField;
    @FXML
    private TextField ibanField;
    @FXML
    private TextField bicField;
    @FXML
    private Button saveButton;
    @FXML
    private Button deletePartButton;
    @FXML
    private Label participantEditWarning;

    private Event event;
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageConf languageConf;
    private final Websocket websocket;

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
     * Initialize listeners
     */
    public void initialize() {
        nameField.textProperty().addListener(this::nameFieldChanged);
    }

    /**
     * Resets the style of the name text field when text changes
     *
     * @param observableValue string visible to user
     * @param oldString old text
     * @param newString new text
     */
    private void nameFieldChanged(ObservableValue<? extends String> observableValue,
                                  String oldString, String newString) {
        if (!oldString.equals(newString)) nameField.setStyle("");
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
                resetFields();
                saveButton.setText(languageConf.get("EditP.save"));
                deletePartButton.setVisible(true);
                Participant p = event.getParticipants().get(index - 1);
                nameField.setText(p.getName());
                emailField.setText(p.getEmailAddress());
                beneficiaryField.setText(p.getBeneficiary());
                ibanField.setText(p.getAccountNumber());
            }
        });

        websocket.registerParticipantChangeListener(
                event,
                this::displayEditParticipantsPage,
                this::displayEditParticipantsPage,
                this::displayEditParticipantsPage
        );

    }

    /**
     * Reset all fields
     */
    private void resetFields() {
        saveButton.setText(languageConf.get("EditP.createParticipant"));
        deletePartButton.setVisible(false);
        nameField.setText("");
        emailField.setText("");
        beneficiaryField.setText("");
        ibanField.setText("");
        bicField.setText("");
        participantEditWarning.setVisible(false);
        nameField.setStyle("");
    }

    /**
     * Handler for the back button
     */
    @FXML
    private void backButtonClicked() {
        mainCtrl.goBackToEventPage(event);
    }

    @FXML
    private void deletePartClicked() {
        int index = chooseParticipant.getSelectionModel().getSelectedIndex();
        Participant part = event.getParticipants().get(index -1);
        String eventID = event.getId();
        server.deleteParticipant(eventID, part.getId());
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
        String beneficiary = beneficiaryField.getText();
        String iban = ibanField.getText();
        String bic = bicField.getText();

        if (index < 0) return;
        if(name.isEmpty()) {
            participantEditWarning.setVisible(true);
            participantEditWarning.setText(languageConf.get("EditP.nameMissing"));
            nameField.setStyle("-fx-border-color: red;");
            return;
        }
        if (index == 0) {
            // create a new participant
            if(event.getParticipants().stream().map(Participant::getName).toList().contains(name)) {
                informNameExists();
                return;
            }
            Participant newP = new Participant(name, email, beneficiary, iban);

            server.createParticipant(event.getId(), newP);
        } else {
            Participant currP = event.getParticipants().get(index - 1);
            if(event.getParticipants().stream()
                    .filter(p -> p.getId() != currP.getId())
                    .map(Participant::getName).toList().contains(name)) {
                informNameExists();
                return;
            }
            currP.setName(name);
            currP.setEmailAddress(email);
            currP.setBeneficiary(beneficiary);
            currP.setAccountNumber(iban);
            server.updateParticipant(event.getId(), currP);
        }
    }

    /**
     * Inform user that a participant with the same name already exists
     */
    private void informNameExists() {
        participantEditWarning.setVisible(true);
        participantEditWarning.setText(languageConf.get("EditP.nameExists"));
        nameField.setStyle("""
                        -fx-border-color: red;
                        -fx-text-inner-color: red""");
    }
}
