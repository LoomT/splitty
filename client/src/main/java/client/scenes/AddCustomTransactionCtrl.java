package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.NoSuchElementException;

public class AddCustomTransactionCtrl {
    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final LanguageConf languageConf;
    @FXML
    private ChoiceBox<String> chooseReceiver;
    @FXML
    private ChoiceBox<String> chooseGiver;
    @FXML
    private ChoiceBox<String> chooseCurrency;
    @FXML
    private TextField amountField;

    private Stage stage;
    private Event event;

    /**
     * @param mainCtrl main controller
     * @param server server utils
     * @param languageConf language config
     */
    @Inject
    public AddCustomTransactionCtrl(MainCtrl mainCtrl, ServerUtils server,
                                    LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.languageConf = languageConf;
    }

    /**
     * Automatically gets called when the app boots up
     * Adds all currency options to the choice box
     */
    public void initialize() {
        chooseCurrency.getItems().addAll(List.of("EUR", "USD", "YEN", "GBP"));
        chooseCurrency.setValue("EUR"); //TODO set the preferred currency here
        DecimalFormat format = new DecimalFormat( "#.0" );

        // only lets the users type decimal numbers
        amountField.setTextFormatter(new TextFormatter<>(c -> {
            if(c.getControlNewText().isEmpty())
                return c;

            ParsePosition parsePosition = new ParsePosition( 0);
            Object object = format.parse(c.getControlNewText(), parsePosition);

            if(object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                return null;
            } else {
                return c;
            }
        }));
    }

    /**
     * Initialize the page with participants of an event
     * @param event event to display
     * @param stage the stage that this scene is in
     */
    public void display(Event event, Stage stage) {
        this.event = event;
        this.stage = stage;
        chooseReceiver.getItems().clear();
        chooseReceiver.getItems().addAll(event.getParticipants()
                .stream().map(Participant::getName).toList());
        chooseGiver.getItems().clear();
        chooseGiver.getItems().addAll(event.getParticipants()
                .stream().map(Participant::getName).toList());
        amountField.setText("");
    }

    /**
     * Saves the transaction
     */
    @FXML
    public void saveClicked() {
        if(!checkFields()) return;
        Participant receiver;
        Participant giver;
        try {
            receiver = event.getParticipants().stream()
                    .filter(p -> p.getName().equals(chooseReceiver.getValue()))
                    .findFirst().orElseThrow();
            giver = event.getParticipants().stream()
                    .filter(p -> p.getName().equals(chooseGiver.getValue()))
                    .findFirst().orElseThrow();

        } catch (NoSuchElementException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("AddCustomTransaction.error"));
            alert.show();
            backClicked();
            return;
        }
        Transaction transaction = new Transaction(giver, receiver,
                Double.parseDouble(amountField.getText()));
        int status = 0;
        try {
            status = server.addTransaction(event.getId(), transaction);
        } catch (ConnectException e) {
            // TODO alert the user
        }
        if(status / 100 != 2) {
            System.out.println("server error: " + status);
        }
        backClicked();
    }

    /**
     * @return true iff all fields are valid
     * TODO make this method smaller
     */
    private boolean checkFields() {
        if(chooseCurrency.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("AddCustomTransaction.errorNoCurrencySelected"));
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        if(chooseReceiver.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    languageConf.get("AddCustomTransaction.warningSelectReceiver"));
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        if(amountField.getText() == null || amountField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    languageConf.get("AddCustomTransaction.warningInputAmount"));
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        if(chooseGiver.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    languageConf.get("AddCustomTransaction.warningSelectGiver"));
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        if(chooseReceiver.getValue().equals(chooseGiver.getValue())) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    languageConf.get("AddCustomTransaction.warningSameParticipants"));
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        if(Double.parseDouble(amountField.getText()) <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    languageConf.get("AddCustomTransaction.warningInvalidAmount"));
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        return true;
    }

    /**
     * closes the window
     */
    @FXML
    public void backClicked() {
        stage.close();
    }
}
