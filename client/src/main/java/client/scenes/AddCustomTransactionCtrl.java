package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.Instant;
import java.util.NoSuchElementException;

public class AddCustomTransactionCtrl {
    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final LanguageConf languageConf;
    private final CurrencyConverter converter;
    private final UserConfig userConfig;
    @FXML
    private ChoiceBox<String> chooseReceiver;
    @FXML
    private ChoiceBox<String> chooseGiver;
    @FXML
    private ComboBox<CommonFunctions.HideableItem<String>> chooseCurrency;
    @FXML
    private TextField amountField;

    private Stage stage;
    private Event event;

    /**
     * @param mainCtrl main controller
     * @param server server utils
     * @param languageConf language config
     * @param converter currency converter
     * @param userConfig user config
     */
    @Inject
    public AddCustomTransactionCtrl(MainCtrlInterface mainCtrl, ServerUtils server,
                                    LanguageConf languageConf,
                                    CurrencyConverter converter, UserConfig userConfig) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.languageConf = languageConf;
        this.converter = converter;
        this.userConfig = userConfig;
    }

    /**
     * Automatically gets called when the app boots up
     * Adds all currency options to the choice box
     */
    public void initialize() {
        CommonFunctions.comboBoxAutoCompletionSupport(converter.getCurrencies(),
                chooseCurrency);
        String cur = userConfig.getCurrency();
        if(!cur.equals("None")) {
            CommonFunctions.HideableItem<String> item =
                    chooseCurrency.getItems().stream()
                            .filter(i -> i.toString().equals(cur)).findFirst().orElse(null);
            chooseCurrency.setValue(item);
        }
        //DecimalFormat format = new DecimalFormat( "#.0" );
        NumberFormat format = NumberFormat.getNumberInstance();

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
        double amount = Double.parseDouble(amountField.getText());
        double convertedAmount;
        try {
            convertedAmount = converter.convert(chooseCurrency.getValue().toString(), "USD",
                    amount, Instant.now());
        } catch (CurrencyConverter.CurrencyConversionException e) {
            return;
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        Transaction transaction = new Transaction(giver, receiver,
                convertedAmount, chooseCurrency.getValue().toString());
        int status;
        try {
            status = server.addTransaction(event.getId(), transaction);
        } catch (ConnectException e) {
            backClicked();
            mainCtrl.handleServerNotFound();
            return;
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

    /**
     * @param scene this scene
     */
    public void initializeShortcuts(Scene scene) {
        MainCtrl.checkKey(scene, this::backClicked, KeyCode.ESCAPE);
        MainCtrl.checkKey(scene, () -> chooseReceiver.show(), chooseReceiver, KeyCode.ENTER);
        MainCtrl.checkKey(scene, () -> chooseGiver.show(), chooseGiver, KeyCode.ENTER);
        MainCtrl.checkKey(scene, () -> chooseCurrency.show(), chooseCurrency, KeyCode.ENTER);
    }
}
