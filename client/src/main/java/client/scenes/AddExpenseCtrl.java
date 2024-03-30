package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
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
import java.util.concurrent.atomic.AtomicInteger;

public class AddExpenseCtrl {

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
    private List<Participant> expPart;
    private boolean splitAll = false;

    /**
     * @param server   server utils instance
     * @param mainCtrl main control instance

     */
    @Inject
    public AddExpenseCtrl(
            ServerUtils server,
            MainCtrl mainCtrl
    ) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        expPart = new ArrayList<>();
    }

    /**
     * Method for displaying the page with a blank expense.
     * @param event the event page to return to
     * @param exp the expense for which the page is displayed
     */
    public void displayAddExpensePage(Event event, Expense exp) {
        equalSplit.setSelected(false);
        partialSplit.setSelected(false);
        equalSplit.setDisable(false);
        populateAuthorChoiceBox(event);
        populateTypeBox();
        purpose.clear();
        amount.clear();
        populateCurrencyChoiceBox();
        date.setValue(LocalDate.now());
        expPart.clear();
        populateSplitPeople(event);
        disablePartialSplitCheckboxes(true);
        equalSplit.setOnAction(e -> {
            //splitAll = false;
            if (equalSplit.isSelected()) {
                //splitAll = true;
                expPart.clear();
                partialSplit.setSelected(false);
                disablePartialSplitCheckboxes(true);
                expPart.addAll(event.getParticipants());
            } else {
                equalSplit.setSelected(true);
            }
        });
        partialSplit.setOnAction(this::handlePartialSplit);


        add.setOnAction(x -> {
            if (exp == null) {
                handleAddButton(event);
            } else {
                editButton(event, exp);
            }
        });
        abort.setOnAction(x -> {
            handleAbortButton(event);
        });
    }


    /**
     * behaviour for the edit button
     * @param ev
     * @param ex
     */
    public void editButton(Event ev, Expense ex) {
        String expParticipant = expenseAuthor.getValue();
        String expPurpose = purpose.getText();
        Double expAmount = Double.parseDouble(amount.getText());
        String expCurrency = currency.getValue();
        LocalDate temp = date.getValue();
        LocalDateTime localDateTime = temp.atStartOfDay();
        Date expDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<Participant> expParticipants = getExpenseParticipants(ev);

        String expType = type.getValue();

        for (Participant p : ex.getExpenseParticipants()) {
            if (p.getName() == expParticipant) {
                ex.setExpenseAuthor(p);
                break;
            }
        }
        //ex.setExpenseAuthor(new Participant(expParticipant));
        ex.setPurpose(expPurpose);
        ex.setAmount(expAmount);
        ex.setCurrency(expCurrency);
        ex.setDate(expDate);
        ex.setExpenseParticipants(expParticipants);
        ex.setType(expType);

        if (expParticipants.size() == 0) {
            alertSelectPart();
            return;
        }

        server.updateExpense(ex.getId(), ev.getId(), ex);
        mainCtrl.goBackToEventPage(ev);
    }

    private List<Participant> getExpenseParticipants(Event ev) {
        List<Participant> expParticipants = new ArrayList<>();
        if (equalSplit.isSelected()) {
            expParticipants.addAll(ev.getParticipants());
        } else if (partialSplit.isSelected()) {
            expParticipants.addAll(getSelectedParticipants(ev));
        }
        return expParticipants;
    }

    private List<Participant> getSelectedParticipants(Event ev) {
        List<Participant> selectedParticipants = new ArrayList<>();
        for (Node node : expenseParticipants.getChildren()) {
            if (node instanceof CheckBox participantCheckBox && participantCheckBox.isSelected()) {
                String participantName = participantCheckBox.getText();
                ev.getParticipants().stream()
                        .filter(p -> p.getName().equals(participantName))
                        .findFirst()
                        .ifPresent(selectedParticipants::add);
            }
        }
        return selectedParticipants;
    }

    /**
     * handle partial splitting
     * @param event current event
     */
    @FXML
    public void handlePartialSplit(ActionEvent event) {
        if (partialSplit.isSelected()) {
            equalSplit.setSelected(false);
            disablePartialSplitCheckboxes(false);
            for (Node node : expenseParticipants.getChildren()) {
                if (node instanceof CheckBox participantCheckBox) {
                    if (participantCheckBox.isSelected()) {
                        String participantName = participantCheckBox.getText();
                        Participant selectedParticipant = new Participant(participantName);
                        expPart.add(selectedParticipant);
                    }
                }
            }
        } else {
            partialSplit.setSelected(true);
        }
    }





    /**
     * @param event
     * Fill the choices for the author of the expense.
     */
    public void populateAuthorChoiceBox(Event event) {
        expenseAuthor.getItems().clear();
        expenseAuthor
            .getItems()
            .addAll(
                event.getParticipants()
                        .stream()
                        .map(Participant::getName)
                        .toList()
            );

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
     * @param ev current event
     */
    public void handleAddButton(Event ev) {
        if (expenseAuthor.getValue() == null ||
                purpose.getText().isEmpty() ||
                amount.getText().isEmpty() ||
                currency.getValue() == null ||
                (!equalSplit.isSelected() && !partialSplit.isSelected()) ||
                date.getValue() == null ||
                type.getValue() == null) {
            alertAllFields();
        } else {
            if (partialSplit.isSelected() && expPart.isEmpty()) {
                alertSelectPart();
            }
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
     * alert to fill all fields
     */
    public void alertAllFields() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Incomplete Fields");
        alert.setHeaderText(null);
        alert.setContentText("Please fill in all fields before adding the expense.");
        alert.showAndWait();
    }

    /**
     * alert for selecting at least one participant
     * when choosing the partial split option
     */
    public void alertSelectPart() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Participants Selected");
        alert.setHeaderText(null);
        alert.setContentText("Please select at least one " +
                "participant for partial splitting.");
        alert.showAndWait();
        return;
    }

    /**
     * handle the behaviour for the abort button
     * @param ev the current event
     */
    public void handleAbortButton(Event ev) {
        resetExpenseFields();
        mainCtrl.goBackToEventPage(ev);
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
     * @param event the current event
     */
    public void populateSplitPeople(Event event) {
        expenseParticipants.getChildren().clear();
        expPart.clear();
        int totalPart = event.getParticipants().size();
        AtomicInteger selectedPart = new AtomicInteger();
        for (Participant participant : event.getParticipants()) {
            CheckBox checkBox = new CheckBox(participant.getName());
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    expPart.add(participant);
                    selectedPart.getAndIncrement();
                } else {
                    expPart.remove(participant);
                    selectedPart.getAndDecrement();
                }
                //updateEqualSplitCheckbox();
            });
            expenseParticipants.getChildren().add(checkBox);
        }
        if (totalPart == selectedPart.get()) {
            equalSplit.setDisable(false);
            equalSplit.setSelected(true);
        }
    }


    private void disablePartialSplitCheckboxes(boolean disable) {
        for (Node node : expenseParticipants.getChildren()) {
            if (node instanceof CheckBox checkBox) {
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
        expPart.clear();
        type.getSelectionModel().clearSelection();
    }

    /**
     * setter for the expense author field
     * @param author
     */
    public void setExpenseAuthor(String author) {
        expenseAuthor.setValue(author);
    }

    /**
     * setter for the purposeText field
     * @param purposeText
     */
    public void setPurpose(String purposeText) {
        purpose.setText(purposeText);
    }

    /**
     * setter for the amountText field
     * @param amountText
     */
    public void setAmount(String amountText) {
        amount.setText(amountText);
    }

    /**
     * setter for the currencyText field
     * @param currencyText
     */
    public void setCurrency(String currencyText) {
        currency.setValue(currencyText);
    }

    /**
     * setter for the expenseDate field
     * @param expenseDate
     */
    public void setDate(LocalDate expenseDate) {
        date.setValue(expenseDate);
    }

    /**
     * setter for the typeText field
     * @param typeText
     */
    public void setType(String typeText) {
        type.setValue(typeText);
    }

    /**
     * setter for button text
     * @param s
     */
    public void setButton(String s) {
        add.setText(s);
    }


    /**
     * Method to set the checkboxes regarding the way in which an expense is split.
     * @param exp
     * @param event
     */
    public void setSplitCheckboxes(Expense exp, Event event) {
        List<Participant> temp = exp.getExpenseParticipants();
        if (temp.size() == event.getParticipants().size()) {
            equalSplit.setSelected(true);
        } else {
            partialSplit.setSelected(true);
            for (Node node : expenseParticipants.getChildren()) {
                if (node instanceof CheckBox checkBox) {
                    checkBox.setDisable(false);
                }
            }
            for (Node node : expenseParticipants.getChildren()) {
                if (node instanceof CheckBox participantCheckBox) {
                    String participantName = participantCheckBox.getText();
                    List<String> names = new ArrayList<>();
                    for (Participant p : exp.getExpenseParticipants()) {
                        names.add(p.getName());
                    }
                    if (names.contains(participantName)) {
                        participantCheckBox.setSelected(true);
                    }
                }
            }
        }
    }
}