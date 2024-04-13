package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.Confirmation;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.net.ConnectException;
import java.util.Optional;

import static client.utils.CommonFunctions.lengthListener;
import static commons.WebsocketActions.*;
import static java.lang.String.format;


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
    private Label warningLabel;
    @FXML
    private Button backButton;

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    private Event event;
    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final Websocket websocket;
    private boolean opened;

    /**
     * @param server       serverutils instance
     * @param mainCtrl     main control instance
     * @param languageConf the language config instance
     * @param websocket    the websocket instance
     */
    @Inject
    public EditParticipantsCtrl(
            ServerUtils server,
            MainCtrlInterface mainCtrl,
            LanguageConf languageConf,
            Websocket websocket
    ) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.websocket = websocket;
        opened = false;
    }

    /**
     * Initialize listeners
     */
    public void initialize() {
        lengthListener(nameField, warningLabel, 30, languageConf.get("EditP.nameLimit"));
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue.length() != newValue.length()) {
                nameField.setStyle("");
            }
        });
        websocket.on(TITLE_CHANGE, title -> {
            if(opened) {
                event.setTitle((String) title);
                eventTitle.setText((String) title);
            }
        });
        websocket.on(ADD_PARTICIPANT, participant -> {
            if(opened)
                displayEditParticipantsPage(event);
        });
        websocket.on(UPDATE_PARTICIPANT, participant -> {
            if(opened)
                displayEditParticipantsPage(event);
        });
        websocket.on(REMOVE_PARTICIPANT, id -> {
            if(opened)
                displayEditParticipantsPage(event);
        });
    }

    /**
     * Call this function when showing this page
     *
     * @param e the event to edit the participants for
     */
    public void displayEditParticipantsPage(Event e) {
        this.event = e;
        opened = true;
        System.out.println("display");
        System.out.println(e);
        eventTitle.setText(e.getTitle());
        addIconsToButtons();

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
                bicField.setText(p.getBic());
            }
        });

    }

    private void addIconsToButtons() {
//        String saveText = saveButton.getText();
//        if (!saveText.startsWith("\uD83D\uDDAB")) {
//            saveButton.setText("\uD83D\uDDAB " + saveText);
//        }

        String backBText = backButton.getText();
        if (!backBText.startsWith("\u2190")) {
            backButton.setText("\u2190 " + backBText);
        }
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
        warningLabel.setVisible(false);
        nameField.setStyle("");
    }

    /**
     * Handler for the back button
     */
    @FXML
    private void backButtonClicked() {
        opened = false;
        mainCtrl.goBackToEventPage(event);
    }

    /**
     * When delete button is pressed, shows a confirmation screen
     * and if confirmed deletes the participant
     */
    @FXML
    private void deletePartClicked() {
        int index = chooseParticipant.getSelectionModel().getSelectedIndex();
        Participant part = event.getParticipants().get(index -1);
        String eventID = event.getId();
        Confirmation confirmation =
                new Confirmation((format(languageConf.get("EditP.deleteConfirmMessage"),
                        part.getName())),
                languageConf.get("Confirmation.areYouSure"), languageConf);
        Optional<ButtonType> result = confirmation.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.YES) {
            try {
                server.deleteParticipant(eventID, part.getId());
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }
        }
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
            warningLabel.setVisible(true);
            warningLabel.setText(languageConf.get("EditP.nameMissing"));
            nameField.setStyle("-fx-border-color: red;");
            return;
        }
        if (index == 0) {
            // create a new participant
            if(event.getParticipants().stream().map(Participant::getName).toList().contains(name)) {
                informNameExists();
                return;
            }
            Participant newP = new Participant(name, email, beneficiary, iban, bic);

            try {
                server.createParticipant(event.getId(), newP);
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }
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
            currP.setBic(bic);
            try {
                server.updateParticipant(event.getId(), currP);
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }
        }
    }

    /**
     * Inform user that a participant with the same name already exists
     */
    private void informNameExists() {
        warningLabel.setVisible(true);
        warningLabel.setText(languageConf.get("EditP.nameExists"));
        nameField.setStyle("""
                        -fx-border-color: red;
                        -fx-text-inner-color: red""");
    }

    /**
     * Initializes the shortcuts for EditParticipants:
     *      Escape: go back
     *      Enter: shows the chooseParticipant choiceBox
     * @param scene scene the listeners are initialised in
     */
    public void initializeShortcuts(Scene scene) {
        MainCtrl.checkKey(scene, this::backButtonClicked, KeyCode.ESCAPE);
        MainCtrl.checkKey(scene, () -> this.chooseParticipant.show(),
                chooseParticipant, KeyCode.ENTER);
    }
}
