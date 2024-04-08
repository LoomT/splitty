package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

import java.net.ConnectException;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static client.utils.CommonFunctions.lengthListener;
import static commons.WebsocketActions.ADD_TAG;

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
    private ComboBox<String> type;

    @FXML
    private Button abort;

    @FXML
    private Button add;
    @FXML
    private Button addTag;
    @FXML
    private Label warningLabel;

    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final Websocket websocket;
    private final LanguageConf languageConf;

    /**
     * @param mainCtrl main control instance
     * @param server   server utils instance
     * @param websocket websocket client
     * @param languageConf language config
     */
    @Inject
    public AddExpenseCtrl(
            MainCtrlInterface mainCtrl,
            ServerUtils server,
            Websocket websocket,
            LanguageConf languageConf
    ) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.websocket = websocket;
        this.languageConf = languageConf;
    }

    /**
     * Runs when app starts
     * Sets a listener for amount field which only let input double amounts
     */
    public void initialize() {
        DecimalFormat format = new DecimalFormat( "#.0" );

        // only lets the users type decimal numbers
        amount.setTextFormatter(new TextFormatter<>(c -> {
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

        lengthListener(purpose, warningLabel, 20, languageConf.get("AddExp.charLimit"));
    }

    /**
     * Method for displaying the page with a blank expense.
     * @param event the event page to return to
     * @param exp the expense for which the page is displayed
     */
    public void displayAddExpensePage(Event event, Expense exp) {
        warningLabel.setVisible(false);
        blockDate();
        setupDateListener();
        date.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });
        equalSplit.setSelected(false);
        partialSplit.setSelected(false);
        equalSplit.setDisable(false);
        populateAuthorChoiceBox(event);
        populateTypeBox(event);
        purpose.clear();
        amount.clear();
        populateCurrencyChoiceBox();
        date.setValue(LocalDate.now());
        populateSplitPeople(event);
        disablePartialSplitCheckboxes(true);
        equalSplit.setOnAction(e -> {
            if (equalSplit.isSelected()) {
                partialSplit.setSelected(false);
                disablePartialSplitCheckboxes(true);
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
        addTag.setOnAction(x -> {
            handeAddTagButton(event);
        });
    }

    /**
     * behaviour for add tag button
     * @param ev
     */
    public void handeAddTagButton(Event ev) {
        mainCtrl.showAddTagPage(ev);
    }

    /**
     * Add an event listener to the date picker to check for future dates.
     */
    private void setupDateListener() {
        date.valueProperty().addListener((observable, oldValue, newValue) -> {
            LocalDate currentDate = LocalDate.now();
            if (newValue != null && newValue.isAfter(currentDate)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(languageConf.get("AddExp.invdate"));
                alert.setHeaderText(null);
                alert.setContentText(languageConf.get("AddExp.invdatemess"));
                alert.showAndWait();
                date.setValue(currentDate);
            }
        });
    }

    /**
     * method for blocking the user fronm choosing a future date
     */
    public void blockDate() {
        date.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });
    }


    /**
     * behaviour for the edit button
     * @param ev event
     * @param ex expense
     */
    public void editButton(Event ev, Expense ex) {
        Expense expense = makeExpense(ev);
        if(expense == null) return;
        expense.setEventID(ex.getEventID());
        expense.setId(ex.getId());
        try {
            server.updateExpense(ex.getId(), ev.getId(), expense);
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        mainCtrl.goBackToEventPage(ev);
    }

    private List<Participant> getExpenseParticipants(Event ev) {
        List<Participant> expParticipants = new ArrayList<>();
        if (equalSplit.isSelected()) {
            expParticipants.addAll(ev.getParticipants());
        } else if (partialSplit.isSelected()) {
            expParticipants.addAll(getSelectedParticipants(ev));
        } else {
            throw new RuntimeException();
        }
        return expParticipants;
    }

    private List<Participant> getSelectedParticipants(Event ev) {
        List<Participant> selectedParticipants = new ArrayList<>();
        for (Node node : expenseParticipants.getChildren()) {
            if (node instanceof CheckBox participantCheckBox && participantCheckBox.isSelected()) {
                String participantName = participantCheckBox.getText();
                selectedParticipants.add(ev.getParticipants().stream()
                        .filter(p -> p.getName().equals(participantName))
                        .findFirst()
                        .orElseThrow());
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
        Expense expense = makeExpense(ev);
        if(expense == null) return;
        try {
            server.createExpense(ev.getId(), expense);
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        resetExpenseFields();
        mainCtrl.goBackToEventPage(ev);
    }

    private Expense makeExpense(Event ev) {
        if (expenseAuthor.getValue() == null ||
                purpose.getText().isEmpty() ||
                amount.getText().isEmpty() ||
                currency.getValue() == null ||
                (!equalSplit.isSelected() && !partialSplit.isSelected()) ||
                date.getValue() == null ||
                type.getValue() == null) {
            alertAllFields();
            return null;
        }
        try {
            List<Participant> participants = getExpenseParticipants(ev);
            double expAmount = Double.parseDouble(amount.getText());
            if(expAmount <= 0) throw new NumberFormatException();

            LocalDate expDate = date.getValue();
            LocalDateTime localDateTime = expDate.atStartOfDay();
            Date expenseDate = Date.from(localDateTime.
                    atZone(ZoneId.systemDefault()).toInstant());
            String expPurpose = purpose.getText();
            String selectedParticipantName = expenseAuthor.getValue();
            Participant selectedParticipant = ev.getParticipants().stream()
                    .filter(participant -> participant.getName().
                            equals(selectedParticipantName))
                    .findFirst().orElseThrow();

            String expCurrency = currency.getValue();

            String expType = type.getValue();
            Expense expense = new Expense(selectedParticipant, expPurpose, expAmount,
                    expCurrency, participants, expType);
            expense.setDate(expenseDate);
            return expense;

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(languageConf.get("AddExp.invamount"));
            alert.setHeaderText(null);
            alert.setContentText(languageConf.get("AddExp.invamountmess"));
            alert.showAndWait();
        }
        return null;
    }

    /**
     * alert to fill all fields
     */
    public void alertAllFields() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(languageConf.get("AddExp.incfields"));
        alert.setHeaderText(null);
        alert.setContentText(languageConf.get("AddExp.incfieldsmess"));
        alert.showAndWait();
    }

    /**
     * alert for selecting at least one participant
     * when choosing the partial split option
     */
    public void alertSelectPart() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(languageConf.get("AddExp.nopart"));
        alert.setHeaderText(null);
        alert.setContentText(languageConf.get("AddExp.nopartmess"));
        alert.showAndWait();
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
     * show the corresponding tags for expense
     *
     * @param ev the current event
     */
    public void populateTypeBox(Event ev) {
        setupTypeComboBox(ev);
    }

    private void setupTypeComboBox(Event ev) {
        type.getItems().clear();
        for (Tag tag : ev.getTags()) {
            type.getItems().add(tag.getName());
        }
        type.setCellFactory(createTypeListCellFactory(ev));
        type.setButtonCell(createTypeListCell(ev));
        websocket.on(ADD_TAG, tag -> {
            String typeName = ((Tag) tag).getName();
            if (!type.getItems().contains(typeName)) {
                type.getItems().add(typeName);
            }
        });

    }

    private Callback<ListView<String>, ListCell<String>> createTypeListCellFactory(Event ev) {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    Tag tag = findTagByName(item, ev.getTags());
                    if (tag != null) {
                        Label label = createLabelWithColor(item, hexToColor(tag.getColor()));
                        setGraphic(label);
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }

    private ListCell<String> createTypeListCell(Event ev) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    Tag tag = findTagByName(item, ev.getTags());
                    if (tag != null) {
                        Label label = createLabelWithColor(item, hexToColor(tag.getColor()));
                        setGraphic(label);
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }

    private Label createLabelWithColor(String text, Color backgroundColor) {
        Label label = new Label(text);
        if (backgroundColor != null) {
            label.setStyle("-fx-background-color: #" + toHexString(backgroundColor)
                    + "; -fx-padding: 5px; -fx-text-fill: white;");
        }
        double textWidth = new Text(text).getLayoutBounds().getWidth();
        label.setMinWidth(textWidth + 10);
        return label;
    }

    private Tag findTagByName(String tagName, List<Tag> tags) {
        for (Tag tag : tags) {
            if (tag.getName().equals(tagName)) {
                return tag;
            }
        }
        return null;
    }


    /**
     * convert from color to string
     * @param color
     * @return the String color
     */
    private String toHexString(Color color) {
        return String.format("%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * convert from string to color
     * @param hexCode
     * @return the Color color
     */
    public static Color hexToColor(String hexCode) {
        if (!hexCode.startsWith("#")) {
            hexCode = "#" + hexCode;
        }

        int red = Integer.parseInt(hexCode.substring(1, 3), 16);
        int green = Integer.parseInt(hexCode.substring(3, 5), 16);
        int blue = Integer.parseInt(hexCode.substring(5, 7), 16);

        return Color.rgb(red, green, blue);
    }

    /**
     * populate the split people list
     * @param event the current event
     */
    public void populateSplitPeople(Event event) {
        expenseParticipants.getChildren().clear();
        int totalPart = event.getParticipants().size();
        AtomicInteger selectedPart = new AtomicInteger();
        for (Participant participant : event.getParticipants()) {
            CheckBox checkBox = new CheckBox(participant.getName());
            checkBox.getStyleClass().add("textFont");
            checkBox.setStyle("-fx-label-padding: 0 10 0 3");
//            checkBox.setOnAction(e -> {
//                if (checkBox.isSelected()) {
//                    expPart.add(participant);
//                    selectedPart.getAndIncrement();
//                } else {
//                    expPart.remove(participant);
//                    selectedPart.getAndDecrement();
//                }
//                //updateEqualSplitCheckbox();
//            });

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