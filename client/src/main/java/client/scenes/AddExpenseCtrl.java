package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import client.utils.Websocket;
import commons.Event;
import commons.Participant;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import commons.Expense;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddExpenseCtrl {
    private boolean splitAll;

    @FXML
    private ChoiceBox<String> expenseAuthor;

    @FXML
    private TextField purpose;

    @FXML
    private TextField amount;

    @FXML
    private ChoiceBox<String> currency;

    @FXML
    private DatePicker date;

    @FXML
    private CheckBox equalSplit;

    @FXML
    private CheckBox partialSplit;

    @FXML
    private TextFlow expenseParticipants;

    @FXML
    private ChoiceBox<String> type;

    @FXML
    private Button abort;

    @FXML
    private Button add;

    private ServerUtils server;
    private MainCtrl mainCtrl;
    private LanguageConf languageConf;
    private Expense expense;
    private List<Participant> selectedParticipants = new ArrayList<>();
    private List<Participant> expPart = new ArrayList<>();

    private Websocket websocket;


    /**
     * @param server   serverutils instance
     * @param mainCtrl main control instance
     * @param languageConf the language config instance
     * @param websocket the websocket instance

     */
    @Inject
    public AddExpenseCtrl(
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
     * Method for displaying the page with a blank expense.
     * @param event the event page to return to
     */
    public void displayAddExpensePage(Event event) {
        populateAuthorChoiceBox(event);
        populateTypeBox();
        purpose.clear();
        amount.clear();
        populateCurrencyChoiceBox();
        date.setValue(LocalDate.now());
        splitAll = false;

        populateSplitPeople(event);
        disablePartialSplitCheckboxes(true);
        equalSplit.setOnAction(e -> {
            if (equalSplit.isSelected()) {
                splitAll = true;
                partialSplit.setSelected(false);
                disablePartialSplitCheckboxes(true);

                expPart.addAll(event.getParticipants());
            }
        });
        partialSplit.setOnAction(this::handlePartialSplit);



        add.setOnAction(x -> {
            handleAddButton(event);
        });

        abort.setOnAction(x -> mainCtrl.goBackToEventPage(event));


    }

    /**
     * handle partial splitting
     * @param event
     */
    @FXML
    public void handlePartialSplit(ActionEvent event) {
        equalSplit.setSelected(false);
        disablePartialSplitCheckboxes(false);
        CheckBox checkBox = (CheckBox) event.getSource();
        if (checkBox.isSelected()) {
            expPart.clear();
            for (Node node : expenseParticipants.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox participantCheckBox = (CheckBox) node;
                    if (participantCheckBox.isSelected()) {
                        String participantName = participantCheckBox.getText();
                        Participant selectedParticipant = new Participant(participantName);
                        expPart.add(selectedParticipant);
                    }
                }
            }
        }
    }


    /**
     * @param event
     * Fill the choices for the author of the expense.
     */
    public void populateAuthorChoiceBox(Event event) {
        if (expenseAuthor.getItems().size() == 0) {
            expenseAuthor
                .getItems()
                .addAll(
                    event.getParticipants()
                            .stream()
                            .map(Participant::getName)
                            .toList()
                );
        }

    }

    /**
     * Fill the choices with currency.
     */
    public void populateCurrencyChoiceBox() {
        List<String> currencies = new ArrayList<>();
        currencies.add("USD");
        currencies.add("EUR");
        currencies.add("GBP");
        currencies.add("JPY");
        currency.getItems().clear();
        currency.getItems().addAll(currencies);
    }


    /**
     * behaviour for add button
     * @param ev
     */
    public void handleAddButton(Event ev) {
        if (expenseAuthor.getValue() == null ||
                purpose.getText().isEmpty() ||
                amount.getText().isEmpty() ||
                currency.getValue() == null ||
                (!equalSplit.isSelected() && !partialSplit.isSelected()) ||
                date.getValue() == null ||
                type.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields before adding the expense.");
            alert.showAndWait();
        } else {
            String amountText = amount.getText();
            try {
                double expAmount = Double.parseDouble(amountText);
                LocalDate expDate = date.getValue();
                LocalDateTime localDateTime = expDate.atStartOfDay();
                Date expenseDate = Date.from(localDateTime.
                        atZone(ZoneId.systemDefault()).toInstant());
                String expPurpose = purpose.getText();
                String selectedParticipantName = expenseAuthor.getValue();
                Participant selectedParticipant = ev.getParticipants().stream()
                        .filter(participant -> participant.getName().
                                equals(selectedParticipantName))
                        .findFirst().orElse(null);
                if (selectedParticipant != null) {
                    String expCurrency = currency.getValue();
                    //expPart.add(selectedParticipant);

                    String expType = type.getValue();
                    Expense expense = new Expense(selectedParticipant, expPurpose, expAmount,
                            expCurrency, expPart, expType);
                    expense.setDate(expenseDate);
                    server.createExpense(ev.getId(), expense);
                    resetExpenseFields();
                    mainCtrl.goBackToEventPage(ev);
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Amount");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid number for the amount.");
                alert.showAndWait();
            }
        }
    }






    /**
     * show corresponding tags for expense
     */
    public void populateTypeBox() {
        if (type.getItems().isEmpty()) {
            type.getItems().add("food");
            type.getItems().add("entrance fees");
            type.getItems().add("travel");
        }
    }

    /**
     * populate the split people list
     * @param event
     */
    public void populateSplitPeople(Event event) {
        expenseParticipants.getChildren().clear();
        selectedParticipants.clear();

        for (Participant participant : event.getParticipants()) {
            CheckBox checkBox = new CheckBox(participant.getName());
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    expPart.add(participant);
                    selectedParticipants.add(participant);
                } else {
                    selectedParticipants.remove(participant);
                    expPart.remove(participant);
                }
            });
            expenseParticipants.getChildren().add(checkBox);
        }
    }


    private void disablePartialSplitCheckboxes(boolean disable) {
        for (Node node : expenseParticipants.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                checkBox.setDisable(disable);
            }
        }
    }

    /**
     * Reset all the fields of an expense after adding it.
     *
     */
    private void resetExpenseFields() {
        purpose.clear();
        amount.clear();
        currency.getSelectionModel().clearSelection();
        date.setValue(LocalDate.now());
        expenseAuthor.getSelectionModel().clearSelection();
        equalSplit.setSelected(false);
        partialSplit.setSelected(false);
        selectedParticipants.clear();
        type.getSelectionModel().clearSelection();
    }
}