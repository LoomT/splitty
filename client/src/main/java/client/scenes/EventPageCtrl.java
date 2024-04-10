package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.ExpenseItem;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import client.utils.currency.CurrencyConverter;
import com.google.inject.Inject;
import commons.Event;
import commons.*;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ConnectException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import static commons.WebsocketActions.ADD_TAG;
import static commons.WebsocketActions.TITLE_CHANGE;


public class EventPageCtrl {

    @FXML
    private Text eventTitle;

    @FXML
    private Text participantText;

    @FXML
    private Button allTab;

    @FXML
    private Button fromTab;

    @FXML
    private Button includingTab;
    private int selectedTab = 0;

    @FXML
    private Button editParticipantsButton;
    @FXML
    private Button backButton;

    @FXML
    private ChoiceBox<String> participantChoiceBox;
    @FXML
    private Button addExpenseButton;

    @FXML
    private VBox expenseVbox;
    @FXML
    private Label inviteCode;
    @FXML
    private Label copiedToClipboardMsg;
    @FXML
    private Button editTitleButton;

    private FadeTransition ft;
    private int selectedParticipantId;

    private final Websocket websocket;
    private final CurrencyConverter converter;
    private final UserConfig userConfig;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final ServerUtils server;
    private Event event;

    /**
     * @return getter for the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param mainCtrl     mainCtrl injection
     * @param languageConf the language config instance
     * @param websocket    the websocket instance
     * @param server       server to be ysed
     * @param converter currency converter
     * @param userConfig user config
     */
    @Inject

    public EventPageCtrl(MainCtrlInterface mainCtrl, LanguageConf languageConf,
                         Websocket websocket, ServerUtils server, CurrencyConverter converter,
                         UserConfig userConfig) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.server = server;
        this.websocket = websocket;
        this.converter = converter;
        this.userConfig = userConfig;
    }

    /**
     * call this function to set all the text on the eventpage to a given event
     *
     * @param e the event to be shown
     */
    public void displayEvent(Event e) {
        this.event = e;
        eventTitle.setText(e.getTitle());

        addIconsToButtons();
        participantChoiceBox.getItems().clear();
        participantChoiceBox.setValue("");
        if (e.getParticipants().isEmpty()) {
            noParticipantsExist();
        } else {
            participantsExist();
            StringBuilder p = new StringBuilder();
            for (int i = 0; i < e.getParticipants().size(); i++) {
                p.append(e.getParticipants().get(i).getName());
                if (i != e.getParticipants().size() - 1) p.append(", ");
            }
            participantText.setText(p.toString());
            participantChoiceBox.getItems().addAll(
                    e.getParticipants().stream().map(Participant::getName).toList()
            );
            participantChoiceBox.setValue(e.getParticipants().getFirst().getName());
            selectedParticipantId = 0;
            String name = e.getParticipants().get(selectedParticipantId).getName();
            fromTab.setText(languageConf.get("EventPage.from") + " " + name);
            includingTab.setText(languageConf.get("EventPage.including") + " " + name);
        }
        participantChoiceBox.setOnAction(event -> {
            selectedParticipantId = participantChoiceBox.getSelectionModel().getSelectedIndex();
            if (selectedParticipantId < 0) return;
            String name = e.getParticipants().get(selectedParticipantId).getName();
            fromTab.setText(languageConf.get("EventPage.from") + " " + name);
            includingTab.setText(languageConf.get("EventPage.including") + " " + name);
            expenseVbox.getChildren().clear();
            populateExpenses();
        });
        handleWS();
        updateExpenses(event);

        copiedToClipboardMsg.setVisible(false);
        inviteCode.setText(String.format(languageConf.get("EventPage.inviteCode"), event.getId()));
    }

    private void addIconsToButtons() {
        editTitleButton.setText("\uD83D\uDD89");

        String addExText = addExpenseButton.getText();
        if (!addExText.startsWith("\u2795")) {
            addExpenseButton.setText("\u2795 " + addExText);
        }

        String editPText = editParticipantsButton.getText();
        if (!editPText.startsWith("\uD83D")) {
            editParticipantsButton.setText("\uD83D\uDD89 " + editPText);
        }

        String backBText = backButton.getText();
        if (!backBText.startsWith("\u2190")) {
            backButton.setText("\u2190 " + backBText);
        }

    }

    /**
     * Runs once after the fxml is loaded
     */
    public void initialize() {
        ft = new FadeTransition(Duration.millis(2000), copiedToClipboardMsg);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(1000));
        ft.setOnFinished(e -> copiedToClipboardMsg.setVisible(false));

        websocket.on(TITLE_CHANGE, title -> {
            event.setTitle(((String) title));
            eventTitle.setText(event.getTitle());
        });
        websocket.on(ADD_TAG, tag -> {
            if (!event.getTags().contains((Tag) tag)) {
                event.getTags().add((Tag) tag);
            }
        });
    }

    /**
     * Registers websocket handlers
     */
    private void handleWS() {
        websocket.registerParticipantChangeListener(
                event,
                this::displayEvent,
                this::displayEvent,
                this::displayEvent
        );
        websocket.registerExpenseChangeListener(
                event,
                this::updateExpenses,
                this::updateExpenses,
                this::updateExpenses
        );
    }


    /**
     * Sets the labels' styles for the case in which no participants exist
     */
    private void noParticipantsExist() {
        participantText.setText(languageConf.get("EventPage.noParticipantsYet"));
        allTab.setStyle("-fx-opacity:0");
        allTab.setDisable(true);
        fromTab.setStyle("-fx-opacity:0");
        fromTab.setDisable(true);
        includingTab.setStyle("-fx-opacity:0");
        includingTab.setDisable(true);
        addExpenseButton.setDisable(true);
    }

    /**
     * Sets the labels' styles for the case in which participants do exist
     */
    private void participantsExist() {
        allTab.setStyle("-fx-opacity:1");
        allTab.setDisable(false);
        fromTab.setStyle("-fx-opacity:1");
        fromTab.setDisable(false);
        includingTab.setStyle("-fx-opacity:1");
        includingTab.setDisable(false);
        addExpenseButton.setDisable(false);
    }

    /**
     * display the expenses
     *
     * @param e event
     */
    public void updateExpenses(Event e) {
        event = e;
        populateExpenses();
    }

    /**
     * action when back button is clicked
     */
    @FXML
    private void backButtonClicked() {
        websocket.disconnect();
        mainCtrl.showStartScreen();
    }

    @FXML
    private void allTabClicked() {
        selectedTab = 0;
        populateExpenses();
        allTab.getStyleClass().add("selectedTabButton");
        fromTab.getStyleClass().remove("selectedTabButton");
        includingTab.getStyleClass().remove("selectedTabButton");

    }

    @FXML
    private void fromTabClicked() {
        selectedTab = 1;
        populateExpenses();
        allTab.getStyleClass().remove("selectedTabButton");
        fromTab.getStyleClass().add("selectedTabButton");
        includingTab.getStyleClass().remove("selectedTabButton");

    }

    @FXML
    private void includingTabClicked() {
        selectedTab = 2;
        populateExpenses();
        allTab.getStyleClass().remove("selectedTabButton");
        fromTab.getStyleClass().remove("selectedTabButton");
        includingTab.getStyleClass().add("selectedTabButton");
    }

    private void populateExpenses() {
        List<Expense> expList = switch (selectedTab) {
            case 0 -> getAllExpenses(event);
            case 1 -> getExpensesFrom(event, extractSelectedName());
            case 2 -> getExpensesIncluding(event, extractSelectedName());
            default -> throw new IllegalStateException("Unexpected value: " + selectedTab);
        };

        expenseVbox.getChildren().clear();
        for (int i = 0; i < expList.size(); i++) {
            Expense e = expList.get(i);
            String partString = "Included participants: " +
                    buildParticipantsList(e.getExpenseParticipants(),
                            event.getParticipants());

            ExpenseItem ei = new ExpenseItem(
                    toString(e),
                    partString,
                    () -> {
                        mainCtrl.handleEditExpense(e, event);
                    },
                    () -> {
                        try {
                            server.deleteExpense(e.getId(), event.getId());
                        } catch (ConnectException ex) {
                            mainCtrl.handleServerNotFound();
                        }
                    }
            );
            expenseVbox.getChildren().add(ei);
        }

    }

    @FXML
    private void editParticipantsClicked() {
        mainCtrl.showEditParticipantsPage(event);
    }

    /**
     * show the add expense page
     */
    @FXML
    private void addExpenseClicked() {
        mainCtrl.showAddExpensePage(event);
    }

    /**
     * create the specific displayed expenses for a listview
     *
     * @param expenses expenses from which to create the list view
     * @param lv       list view
     * @param ev       event
     */
    public void createExpenses(List<Expense> expenses, ListView<String> lv, Event ev) {
        lv.setCellFactory(param -> new ListCell<>() {
            private final Button editButton = new Button("\uD83D\uDD89");
            private final Button removeButton = new Button("\u274C");
            private final HBox buttonBox = new HBox();
            private final StackPane stackPane = new StackPane();

            {
                stackPane.setAlignment(Pos.CENTER_LEFT);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.getChildren().addAll(editButton, removeButton);
                stackPane.getChildren().addAll(new Text(), buttonBox);
                editButton.setOnAction(event -> {
                    int index = getIndex();
                    Expense expense = expenses.get(index);
                    mainCtrl.handleEditExpense(expense, ev);
                });
                removeButton.setOnAction(event -> {
                    int index = getIndex();
                    Expense expense = expenses.get(index);
                    try {
                        server.deleteExpense(expense.getId(), ev.getId());
                    } catch (ConnectException e) {
                        mainCtrl.handleServerNotFound();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    stackPane.getChildren().set(0, new Text(item));
                    setGraphic(stackPane);
                }
            }
        });
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Expense expense : expenses) {
            String expenseString = toString(expense);
            String temp = buildParticipantsList(expense.getExpenseParticipants(),
                    ev.getParticipants());
            items.add(expenseString + "\n" + "Included participants:   " + temp);
        }
        lv.setItems(items);
    }


    private String buildParticipantsList(List<Participant> participants,
                                         List<Participant> allParticipants) {
        StringBuilder participantsList = new StringBuilder();

        int count = participants.size();
        if (count == allParticipants.size()) {
            participantsList.append("all");
        } else {
            for (int i = 0; i < count; i++) {
                participantsList.append(participants.get(i).getName());
                if (i < count - 1) {
                    participantsList.append(", ");
                }
            }
        }
        return participantsList.toString();
    }

    /**
     * return form for displaying the expenses in the event page
     *
     * @param exp the expense
     * @return human-readable form
     */
    public String toString(Expense exp) {
        String date = DateTimeFormatter.ISO_LOCAL_DATE
                .format(exp.getDate().toInstant()
                        .atZone(TimeZone.getDefault().toZoneId()));

        double amount = exp.getAmount();
        String currency = exp.getCurrency().toUpperCase();
        try {
            if(!userConfig.getCurrency().equals("NONE")) {
                amount = converter.convert(exp.getCurrency(), userConfig.getCurrency(),
                        amount, exp.getDate().toInstant());
                currency = userConfig.getCurrency().toUpperCase();
            }

        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return "connection issue";
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("Currency.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            alert.showAndWait();
            return "broke";
        }
        NumberFormat formater = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formater.setMaximumFractionDigits(2);
        formater.setCurrency(Currency.getInstance(currency));
        String formattedAmount = formater.format(amount);

        return date + "     " + exp.getExpenseAuthor().getName() + " " +
                languageConf.get("AddExp.paid") + " " + formattedAmount +
                " " + languageConf.get("AddExp.for") + " " +
                exp.getPurpose();
    }

    /**
     * return all expenses
     *
     * @param ev event
     * @return all expenses
     */
    public List<Expense> getAllExpenses(Event ev) {
        return ev.getExpenses();
    }

    /**
     * return all expenses from a certain person
     *
     * @param ev   event
     * @param name name of participant
     * @return all expenses from a certain person
     */
    public List<Expense> getExpensesFrom(Event ev, String name) {
        List<Expense> allExp = ev.getExpenses();
        List<Expense> temp = new ArrayList<>();
        for (Expense exp : allExp) {
            if (exp.getExpenseAuthor().getName().equals(name)) {
                temp.add(exp);
            }
        }
        return temp;
    }

    /**
     * Displays edit title pop-up
     */
    public void changeTitle() {
        mainCtrl.showEditTitle(this.event);
    }

    /**
     * return all expenses including a certain person
     *
     * @param ev   event
     * @param name name of participant
     * @return all expenses including a certain person
     */
    public List<Expense> getExpensesIncluding(Event ev, String name) {
        List<Expense> allExp = ev.getExpenses();
        List<Expense> temp = new ArrayList<>();
        for (Expense exp : allExp) {
            List<String> participantNames = new ArrayList<>();
            for (Participant p : exp.getExpenseParticipants()) {
                participantNames.add(p.getName());
            }
            if (participantNames.contains(name)) {
                temp.add(exp);
            }
        }
        return temp;
    }

    /**
     * extract the selected name from the choice box
     *
     * @return the name
     */
    public String extractSelectedName() {
        return participantChoiceBox.getValue();
    }

    /**
     * Gets called when invite code in the event overview is clicked
     * Copies the invite code to the system clipboard
     * and displays a message informing that the code was copied which fades out
     */
    @FXML
    public void inviteCodeClicked() {
        StringSelection stringSelection = new StringSelection(event.getId());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        ft.stop();
        copiedToClipboardMsg.setVisible(true);
        copiedToClipboardMsg.setOpacity(1.0);
        ft.play();
    }

    /**
     * Show the openDebts page with the current event
     */
    @FXML
    public void openDebtsPage() {
        mainCtrl.showDebtsPage(event);
    }
}