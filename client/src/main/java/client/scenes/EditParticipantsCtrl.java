package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.Confirmation;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.ConnectException;
import java.util.Optional;

import static client.utils.CommonFunctions.lengthListener;
import static commons.WebsocketActions.TITLE_CHANGE;
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
    private FadeTransition ft;

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
            if(event != null)
                event.setTitle((String) title);
            eventTitle.setText((String) title);
        });
        warningLabel.setVisible(false);
        ft = new FadeTransition(Duration.millis(3000), warningLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(0));
        ft.setOnFinished(e -> warningLabel.setVisible(false));
    }

    /**
     * Call this function when showing this page
     *
     * @param e the event to edit the participants for
     */
    public void displayEditParticipantsPage(Event e) {
        this.event = e;
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

        websocket.registerParticipantChangeListener(
                event,
                this::displayEditParticipantsPage,
                this::displayEditParticipantsPage,
                this::displayEditParticipantsPage
        );

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
        nameField.setStyle("");
    }

    /**
     * Handler for the back button
     */
    @FXML
    private void backButtonClicked() {
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

        boolean edited = false;

        if (index < 0) return;
        if(name.isEmpty()) {
            warningLabel.setVisible(true);
            warningLabel.setText(languageConf.get("EditP.nameMissing"));
            nameField.setStyle("-fx-border-color: red;");
            return;
        }
        if (index == 0) {
            if (creatingParticipant(name, email, beneficiary, iban, bic)) return;
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
                edited = true;

            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }
        }
        confirmationLabeling(edited);
    }

    /**
     * Crating the participant
     * @param name name of the participant
     * @param email email of the participant
     * @param beneficiary beneficiary of the participant
     * @param iban iban of the participant
     * @param bic bic of the participant
     * @return true if the participant already exists
     */
    private boolean creatingParticipant(
            String name,
            String email,
            String beneficiary,
            String iban,
            String bic) {
        if(event.getParticipants().stream().map(Participant::getName).toList().contains(name)) {
            informNameExists();
            return true;
        }
        Participant newP = new Participant(name, email, beneficiary, iban, bic);

        try {

            server.createParticipant(event.getId(), newP);
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
        }
        return false;
    }

    /**
     * Display a confirmation message
     *
     * @param edited whether the participant was edited
     */
    private void confirmationLabeling(boolean edited) {
        if(edited) {
            warningLabel.setVisible(true);
            warningLabel.setOpacity(1);
            warningLabel.setText(languageConf.get("EditP.editConfirmation"));
            ft.play();
        }else{
            warningLabel.setVisible(true);
            warningLabel.setOpacity(1);
            warningLabel.setText(languageConf.get("EditP.createConfirmation"));
            ft.play();
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
