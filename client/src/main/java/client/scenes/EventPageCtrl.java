package client.scenes;


import client.utils.LanguageConf;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.WebsocketActions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class EventPageCtrl {

    @FXML
    private Text eventTitle;

    @FXML
    private Text participantText;

    @FXML
    private Tab allTab;

    @FXML
    private Tab fromTab;

    @FXML
    private Tab includingTab;

    @FXML
    private ChoiceBox<String> participantChoiceBox;
    @FXML
    private Button addExpenseButton;

    @FXML
    private ListView<String> allListView;

    @FXML
    private ListView<String> fromListView;

    @FXML
    private ListView<String> includingListView ;


    private int selectedParticipantId;

    private final Websocket websocket;
    private final MainCtrl mainCtrl;
    private final LanguageConf languageConf;
    private Event event;

    /**
     * @return getter for the event
     */
    public Event getEvent() {
        return event;
    }
    private List<Expense> fromExpenses;
    private List<Expense> includingExpenses;

    /**
     * @param mainCtrl     mainCtrl injection
     * @param languageConf the language config instance
     * @param websocket the websocket instance
     */
    @Inject
    public EventPageCtrl(
        MainCtrl mainCtrl,
        LanguageConf languageConf,
        Websocket websocket
    ) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;

        this.websocket = websocket;
        websocket.on(WebsocketActions.TITLE_CHANGE, (newTitle) -> changeTitle((String) newTitle));
    }

    /**
     * call this function to set all the text on the eventpage to a given event
     *
     * @param e the event to be shown
     */
    public void displayEvent(Event e) {
        this.event = e;
        eventTitle.setText(e.getTitle());
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
            fromListView.getItems().clear();
            includingListView.getItems().clear();
            fromExpenses = getExpensesFrom(e, name);
            includingExpenses = getExpensesIncluding(e, name);
            createExpenses(fromExpenses, fromListView, e);
            createExpenses(includingExpenses, includingListView, e);
        });
        handleWS();
        displayExpenses(event);
    }
    private void handleWS() {
        websocket.registerParticipantChangeListener(
                event,
                this::displayEvent,
                this::displayEvent,
                this::displayEvent
        );
        websocket.registerExpenseChangeListener(
                event,
                this::displayExpenses,
                this::displayExpenses,
                this::displayExpenses
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
     * @param e event
     */
    public void displayExpenses(Event e) {
        String selectedName = extractSelectedName();
        tabSelectionChanged(e, selectedName);
    }


    /**
     * Changes the title of the event
     *
     * @param newTitle new title of the event
     */
    public void changeTitle(String newTitle) {
        event.setTitle(newTitle);
        eventTitle.setText(newTitle);
    }

    /**
     * action when back button is clicked
     */
    @FXML
    private void backButtonClicked() {
        websocket.disconnect();
        mainCtrl.showStartScreen();
    }

    /**
     * actions for when the tab selection is changed
     * @param e event
     * @param selectedParticipantName selected participant name
     */
    @FXML
    public void tabSelectionChanged(Event e, String selectedParticipantName) {
        List<Expense> allExpenses = getAllExpenses(e);
        fromExpenses = getExpensesFrom(e, selectedParticipantName);
        includingExpenses = getExpensesIncluding(e, selectedParticipantName);
        createExpenses(allExpenses, allListView, e);
        createExpenses(fromExpenses, fromListView, e);
        createExpenses(includingExpenses, includingListView, e);
    }


    @FXML
    private void sendInvitesClicked() {

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
     * @param expenses expenses from which to create the list view
     * @param lv list view
     * @param ev event
     */
    public void createExpenses(List<Expense> expenses, ListView<String> lv, Event ev) {
        lv.setCellFactory(param -> new ListCell<>() {
            private final Button editButton = new Button("\uD83D\uDD89");
            private final StackPane stackPane = new StackPane();

            {
                stackPane.setAlignment(Pos.CENTER_LEFT);
                StackPane.setAlignment(editButton, Pos.CENTER_RIGHT);
                stackPane.getChildren().add(editButton);
                editButton.setOnAction(event -> {
                    int index = getIndex();
                    System.out.println(index);
                    Expense expense = expenses.get(index);
                    mainCtrl.handleEditExpense(expense, ev);
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
                    stackPane.getChildren().clear();
                    stackPane.getChildren().addAll(new Text(item), editButton);
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
     * @param exp the expense
     * @return human-readable form
     */
    public String toString(Expense exp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(exp.getDate());
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        NumberFormat currencyFormatter = switch (exp.getCurrency()) {
            case "USD" -> NumberFormat.getCurrencyInstance(Locale.US);
            case "EUR" -> NumberFormat.getCurrencyInstance(Locale.GERMANY);
            case "GBP" -> NumberFormat.getCurrencyInstance(Locale.UK);
            case "JPY" -> NumberFormat.getCurrencyInstance(Locale.JAPAN);
            default -> NumberFormat.getCurrencyInstance(Locale.getDefault());
        };

        String formattedAmount = currencyFormatter.format(exp.getAmount());

        return dayOfMonth + "." + month + "." + year + "     " +
                exp.getExpenseAuthor().getName() + " " + languageConf.get("AddExp.paid") +
                " " + formattedAmount + " " + languageConf.get("AddExp.for") + " " +
                exp.getPurpose();
    }

    /**
     * return all expenses
     * @param ev event
     * @return all expenses
     */
    public List<Expense> getAllExpenses(Event ev) {
        return ev.getExpenses();
    }

    /**
     * return all expenses from a certain person
     * @param ev event
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
     * return all expenses including a certain person
     * @param ev event
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
     *extract the selected name from the choice box
     * @return the name
     */
    public String extractSelectedName() {
        return participantChoiceBox.getValue();
    }

}