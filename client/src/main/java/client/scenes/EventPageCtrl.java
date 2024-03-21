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
            participantText.setText(languageConf.get("EventPage.noParticipantsYet"));
            allTab.setStyle("-fx-opacity:0");
            allTab.setDisable(true);
            fromTab.setStyle("-fx-opacity:0");
            fromTab.setDisable(true);
            includingTab.setStyle("-fx-opacity:0");
            includingTab.setDisable(true);
            //addExpenseButton.setDisable(true);
        } else {
            allTab.setStyle("-fx-opacity:1");
            allTab.setDisable(false);
            fromTab.setStyle("-fx-opacity:1");
            fromTab.setDisable(false);
            includingTab.setStyle("-fx-opacity:1");
            includingTab.setDisable(false);
            addExpenseButton.setDisable(false);
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
            createExpensesFrom(e, name);
            createExpensesIncluding(e, name);
        });
        websocket.connect(e.getId());
        registerExpenseChangeListener();
    }

    /**
     * display the expenses
     * @param e
     */
    public void displayExpenses(Event e) {
        String selectedName = extractSelectedName();
        tabSelectionChanged(e, selectedName);

//        createExpensesFrom(e, selectedName);
//        createExpensesIncluding(e, selectedName);
    }

    /**
     * Registers all the change listeners on WS if they're not registered already
     *
     */

    private void registerExpenseChangeListener() {
        if (previousEventId.equals(event.getId())) return;
        previousEventId = event.getId();
        websocket.resetAction(WebsocketActions.ADD_EXPENSE);
        websocket.resetAction(WebsocketActions.UPDATE_EXPENSE);
        websocket.resetAction(WebsocketActions.REMOVE_EXPENSE);
        websocket.on(WebsocketActions.ADD_EXPENSE, (Object exp) -> {
            Expense expense = (Expense) exp;
            event.getExpenses().add(expense);
            displayEvent(event);
        });
        websocket.on(WebsocketActions.UPDATE_EXPENSE, (Object exp) -> {
            Expense expense = (Expense) exp;
            int index = -1;
            for (int i = 0; i < event.getExpenses().size(); i++) {
                Expense curr = event.getExpenses().get(i);
                if (curr.getExpenseID() == expense.getExpenseID()) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The updated expense's ID ("
                        + expense.getExpenseID()+
                        ") does not match with any ID's of the already existing expenses");
            }
            event.getExpenses().remove(index);
            event.getExpenses().add(index, expense);
            displayEvent(event);
        });
        websocket.on(WebsocketActions.REMOVE_EXPENSE, (Object exp) -> {
            long expId = (long) exp;
            int index = -1;
            for (int i = 0; i < event.getExpenses().size(); i++) {
                Expense curr = event.getExpenses().get(i);
                if (curr.getExpenseID() == expId) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("The deleted expense's ID ("
                        + expId+
                        ") does not match with any ID's of the already existing expenses");
            }
            event.getExpenses().remove(index);
            displayEvent(event);
        });

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

        try {
            Tab selectedTab = expenseList.getSelectionModel().getSelectedItem();

            if (selectedTab != null) {

                if (selectedTab == allTab) {
                    createAllExpenses(e);
                    createExpensesFrom(e, selectedParticipantName);
                    createExpensesIncluding(e, selectedParticipantName);
                } else if (selectedTab == fromTab) {
                    fromListView.getItems().clear();
                    createExpensesFrom(e, selectedParticipantName);
                } else if (selectedTab == includingTab) {
                    includingListView.getItems().clear();
                    createExpensesIncluding(e, selectedParticipantName);
                }


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
     * expenses for all tab
     * @param event
     */
    public void createAllExpenses(Event event) {
        String name = extractSelectedName();

        allExpenses = getAllExpenses(event);

        ObservableList<String> items = FXCollections.observableArrayList();

        for (Expense expense : allExpenses) {
            String expenseString = expense.toString();
            items.add(expenseString);

            if (expense.getExpenseAuthor().getName().equals(name)) {
                if (!fromListView.getItems().contains(expenseString)) {
                    fromListView.getItems().add(expenseString);
                }
            }
        }
        allListView.setItems(items);
    }

    /**
     * expenses for from tab
     * @param event
     * @param personName
     */
    public void createExpensesFrom(Event event, String personName) {
        fromExpenses = getExpensesFrom(event, personName);

        ObservableList<String> items = FXCollections.observableArrayList();

        for (Expense expense : fromExpenses) {
            String expenseString = expense.toString();
            items.add(expenseString);
        }
        fromListView.setItems(items);
        if (!allListView.getItems().contains(String.valueOf(items.getLast()))) {
            allListView.getItems().add(String.valueOf(items.getLast()));
        }
    }

    /**
     * expenses for including tab
     * @param event
     * @param personName
     */
    public void createExpensesIncluding(Event event, String personName) {
        includingExpenses = getExpensesIncluding(event, personName);

        ObservableList<String> items = FXCollections.observableArrayList();

        for (Expense expense : includingExpenses) {
            String expenseString = expense.toString();
            items.add(expenseString);
        }
        includingListView.setItems(items);
        if (!allListView.getItems().contains(String.valueOf(items.getLast()))) {
            allListView.getItems().add(String.valueOf(items.getLast()));
        }

        if (!fromListView.getItems().contains(String.valueOf(items.getLast()))) {
            fromListView.getItems().add(String.valueOf(items.getLast()));
        }
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