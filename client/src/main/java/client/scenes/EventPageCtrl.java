package client.scenes;


import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.WebsocketActions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;


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
    private TabPane expenseList;
    @FXML
    private ListView<String> allListView;

    @FXML
    private ListView<String> fromListView;

    @FXML
    private ListView<String> includingListView ;


    private int selectedParticipantId;

    private Websocket websocket;

    private ServerUtils server;
    private MainCtrl mainCtrl;
    private LanguageConf languageConf;
    private Event event;
    private List<Expense> allExpenses;
    private List<Expense> fromExpenses;
    private List<Expense> includingExpenses;
    private String selectedName;

    private String previousEventId = "";

    /**
     * @param server       server utils injection
     * @param mainCtrl     mainCtrl injection
     * @param languageConf the language config instance
     * @param websocket the websocket instance
     */
    @Inject
    public EventPageCtrl(
        ServerUtils server,
        MainCtrl mainCtrl,
        LanguageConf languageConf,
        Websocket websocket
    ) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;

        this.websocket = websocket;
        websocket.on(WebsocketActions.TITLE_CHANGE, (newTitle) -> {
            event.setTitle(((String) newTitle));
            eventTitle.setText(((String) newTitle));
        });


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
            participantChoiceBox.setValue(e.getParticipants().get(0).getName());
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
//            createExpensesFrom(e, name);
//            createExpensesIncluding(e, name);

            fromExpenses = getExpensesFrom(e, name);
            includingExpenses = getExpensesIncluding(e, name);
            createExpenses(fromExpenses, fromListView);
            createExpenses(includingExpenses, includingListView);

        });
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

    private void handleWS() {

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
     * @param e
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
     * @param e
     * @param selectedParticipantName
     */
    @FXML
    public void tabSelectionChanged(Event e, String selectedParticipantName) {
        allExpenses = getAllExpenses(event);
        fromExpenses = getExpensesFrom(e, selectedParticipantName);
        includingExpenses = getExpensesIncluding(e, selectedParticipantName);
        createExpenses(allExpenses, allListView);
        createExpenses(fromExpenses, fromListView);
        createExpenses(includingExpenses, includingListView);
//        createAllExpenses(e);
//        createExpensesFrom(e, selectedParticipantName);
//        createExpensesIncluding(e, selectedParticipantName);
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

    public void createExpenses(List<Expense> expenses, ListView<String> lv) {
        ObservableList<String> items = lv.getItems();
        for (Expense expense : expenses) {
            String expenseString = expense.toString();
            if (!items.contains(expenseString)) {
                items.add(expenseString);
            }
        }
        lv.setItems(items);
    }



    /**
     * return all expenses
     * @param ev
     * @return all expenses
     */
    public List<Expense> getAllExpenses(Event ev) {
        return ev.getExpenses();
    }

    /**
     * return all expenses from a certain person
     * @param ev
     * @param name
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
     * @param ev
     * @param name
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
        System.out.println(participantChoiceBox.getValue());
        return participantChoiceBox.getValue();
    }

}