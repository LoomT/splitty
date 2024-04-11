package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.Confirmation;
import client.components.ExpandedOpenDebtsListItem;
import client.components.SettledDebtsListItem;
import client.components.ShrunkOpenDebtsListItem;
import client.utils.*;
import client.utils.currency.CurrencyConverter;
import commons.*;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static commons.WebsocketActions.*;

public class OpenDebtsPageCtrl {

    @FXML
    private VBox allDebtsPane;
    @FXML
    private Button openDebtsBtn;
    @FXML
    private Button settledDebtsBtn;

    private final LanguageConf languageConf;
    private Event event;

    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;
    private final Websocket websocket;
    private final CurrencyConverter converter;
    private final UserConfig userConfig;
    private final EmailService emailService;
    private Map<String, Double> participantDebtMap;

    private enum Tab{OPEN, SETTLED}
    private Tab tab;

    /**
     * Constructor
     *
     * @param serverUtils  the server utils
     * @param mainCtrl     the main controller
     * @param languageConf language conf of the user
     * @param websocket    websocket
     * @param converter currency converter
     * @param userConfig user config
     * @param emailService email service
     */
    @Inject
    public OpenDebtsPageCtrl(
            ServerUtils serverUtils,
            MainCtrlInterface mainCtrl,
            LanguageConf languageConf,
            Websocket websocket,
            CurrencyConverter converter,
            UserConfig userConfig,
            EmailService emailService) {

        this.server = serverUtils;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.websocket = websocket;
        this.converter = converter;
        this.userConfig = userConfig;
        this.emailService = emailService;
        participantDebtMap = new HashMap<>();
        tab = Tab.OPEN;
    }

    /**
     * Initialize the websockets for the OpenDebtsPageCtrl.
     */
    public void initialize() {
        websocket.on(WebsocketActions.ADD_TRANSACTION,
                transaction -> {
                    if(tab == Tab.SETTLED)
                        displaySettledDebts();
                    else displayOpenDebtsPage(event);
                });

        websocket.on(WebsocketActions.REMOVE_TRANSACTION,
                id -> {
                    if(tab == Tab.SETTLED)
                        displaySettledDebts();
                    else displayOpenDebtsPage(event);
                });
    }

    /**
     * Register websocket listeners and set tab to open
     */
    public void registerWS() {
        websocket.on(ADD_EXPENSE, (exp) -> displayOpenDebtsPage(event));
        websocket.on(UPDATE_EXPENSE, (exp) -> displayOpenDebtsPage(event));
        websocket.on(REMOVE_EXPENSE, (exp) -> displayOpenDebtsPage(event));
        websocket.on(ADD_PARTICIPANT, (participant) -> displayOpenDebtsPage(event));
        websocket.on(UPDATE_PARTICIPANT, (participant) -> displayOpenDebtsPage(event));
        websocket.on(REMOVE_PARTICIPANT, (participant) -> displayOpenDebtsPage(event));
        tab = Tab.OPEN;
        settledDebtsBtn.getStyleClass().remove("selectedTabButton");
        openDebtsBtn.getStyleClass().remove("selectedTabButton");
        openDebtsBtn.getStyleClass().add("selectedTabButton");
    }

    /**
     * Displays the open debts page
     *
     * @param event the event
     */
    public void displayOpenDebtsPage(Event event) {
        if(tab == Tab.SETTLED) return;
        this.event = event;

        Map<String, Double> map = new HashMap<>();
        Map<Participant, Map<Participant, Double>> partToPartMap = new HashMap<>();

        for (Participant p1 : event.getParticipants()) {
            partToPartMap.put(p1, new HashMap<>());
            for (Participant p2 : event.getParticipants()) {
                partToPartMap.get(p1).put(p2, 0.0);
            }
        }

        event.getParticipants().forEach(x -> map.put(x.getName(), 0.0));
        allDebtsPane.getChildren().clear();
        if (event.getExpenses().isEmpty()) return;
        initializePage(map, partToPartMap);

        if (map.equals(participantDebtMap)) return;
        participantDebtMap = map;
    }

    /**
     * Initializes the graph and the open debts
     *
     * @param graphMap map to be used to populate the graph
     * @param debtMap  map to be used to populate the debts
     */
    public void initializePage(Map<String, Double> graphMap,
                               Map<Participant, Map<Participant, Double>> debtMap) {
        for (Expense e : event.getExpenses()) {
            for (Participant p : e.getExpenseParticipants()) {
                double cost = e.getAmount() / e.getExpenseParticipants().size();
                graphMap.put(p.getName(), graphMap.get(p.getName()) + cost);
                if (e.getExpenseAuthor().equals(p)) continue;

                Map<Participant, Double> temp = debtMap.get(e.getExpenseAuthor());

                temp.put(p, temp.get(p) + cost);
            }
        }
        for (Participant receiver : debtMap.keySet()) {
            for (Participant giver : debtMap.get(receiver).keySet()) {
                double cost = debtMap.get(receiver).get(giver)
                        - event.getTransactions().stream().
                        filter(x -> x.getGiver().equals(giver) && x.getReceiver().
                                equals(receiver))
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                debtMap.get(receiver).put(giver, cost);
            }
        }
        minCashFlow(debtMap, event);
    }

    /**
     * Given a set of debts with Map<Participant, Map<Participant,Double>> calculates the
     * minimum cash flow to settle all debts.
     *
     * @param debtMap Map of all the debts using an adjacency map data structure
     * @param event   event that the debts occur in
     */
    public void minCashFlow(Map<Participant, Map<Participant, Double>> debtMap, Event event) {
        Map<Participant, Double> map = new HashMap<>();
        event.getParticipants().forEach(p -> map.put(p, 0.0));
        for (Participant p : event.getParticipants()) {
            for (Participant i : event.getParticipants()) {
                if (p.equals(i)) continue;
                map.put(p, map.get(p) + debtMap.get(i).get(p) - debtMap.get(p).get(i));
            }
        }
        recursionCalculate(map);
    }

    /**
     * Recursively calculates the minimum amount of transactions needed to settle
     * all debts with a maximum on n-1 where n is the number of participants.
     * Works via finding the maximum debit and credit and decrementing them
     * from each other until they are both zero.
     *
     * @param debtMap map of all the participants and minimum cash flows that conclude all debts
     */
    public void recursionCalculate(Map<Participant, Double> debtMap) {
        Participant maxCredit = getMax(debtMap);
        Participant maxDebit = getMin(debtMap);
        if (debtMap.get(maxDebit) == 0 && debtMap.get(maxCredit) == 0)
            return;

        //Check to stop a stackoverflow error if a floating point issue has occurred
        if((debtMap.get(maxDebit) == 0 || debtMap.get(maxCredit) == 0)
                && Math.floor(debtMap.get(maxDebit)) == 0
                || Math.floor(debtMap.get(maxCredit)) == 0)
            return;

        double min = Math.min(-debtMap.get(maxDebit), debtMap.get(maxCredit));
        debtMap.put(maxCredit, debtMap.get(maxCredit) - min);
        debtMap.put(maxDebit, debtMap.get(maxDebit) + min);
        recursionCalculate(debtMap);

        allDebtsPane.getChildren().add(
                new ShrunkOpenDebtsListItem(
                        new Transaction(maxCredit, maxDebit, min, userConfig.getCurrency()),
                        languageConf, this::resizeOpenDebtItem,
                        this::settleDebtClicked, converter, mainCtrl));
    }

    /**
     * resizes the debtItems depending on their size.
     *
     * @param item OpenDebts item to be resized
     */
    public void resizeOpenDebtItem(Node item) {
        int index = -1;
        List<Node> list = allDebtsPane.getChildren();
        for (int i = 0; i < list.size(); i++) {
            if (item.equals(list.get(i))) {
                index = i;
            }
        }
        if (index == -1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(languageConf.get("unexpectedError"));
            alert.showAndWait();
            return;
        }
        if (item.getClass() == ShrunkOpenDebtsListItem.class) {
            ShrunkOpenDebtsListItem oldItem = (ShrunkOpenDebtsListItem) list.get(index);
            list.set(index, new ExpandedOpenDebtsListItem(oldItem.getTransaction(),
                    languageConf, this::resizeOpenDebtItem,
                    this::settleDebtClicked, converter, mainCtrl, emailService));
        } else {
            ExpandedOpenDebtsListItem oldItem = (ExpandedOpenDebtsListItem) list.get(index);
            allDebtsPane.getChildren().set(index, new ShrunkOpenDebtsListItem(
                    oldItem.getTransaction(),
                    languageConf, this::resizeOpenDebtItem,
                    this::settleDebtClicked, converter, mainCtrl));
        }
    }

    /**
     * Commits the transaction
     *
     * @param transaction transaction to settle
     */
    public void settleDebtClicked(Transaction transaction) {
        int status = 0;
        try {
            status = server.addTransaction(event.getId(), transaction);
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        if(status / 100 != 2) {
            System.out.println("server error: " + status);
        }
    }

    /**
     * Handles the back button click event functionality
     */
    @FXML
    public void backButtonClicked() {
        mainCtrl.goBackToEventPage(event);
    }

    /**
     * display custom transaction screen
     */
    @FXML
    public void addCustomTransactionClicked() {
        mainCtrl.showAddCustomTransaction(event);
    }

    /**
     * open debts tab clicked
     */
    @FXML
    public void openDebtsClicked() {
        if (tab == Tab.OPEN) return;
        tab = Tab.OPEN;
        openDebtsBtn.getStyleClass().add("selectedTabButton");
        settledDebtsBtn.getStyleClass().remove("selectedTabButton");
        displayOpenDebtsPage(event);
    }

    /**
     * settled debts tab clicked
     */
    @FXML
    public void settledDebtsClicked() {
        if(tab == Tab.SETTLED) return;
        tab = Tab.SETTLED;
        openDebtsBtn.getStyleClass().remove("selectedTabButton");
        settledDebtsBtn.getStyleClass().add("selectedTabButton");
        displaySettledDebts();
    }

    private void displaySettledDebts() {
        allDebtsPane.getChildren().clear();
        List<Transaction> sorted = event.getTransactions().stream().sorted().toList();
        for(Transaction t : sorted) {
            allDebtsPane.getChildren().add(
                    new SettledDebtsListItem(t, userConfig, languageConf,
                            this::cancelTransaction, converter, mainCtrl));
        }
    }

    private void cancelTransaction(Transaction transaction) {
        Confirmation confirmation =
                new Confirmation(languageConf.get("OpenDebts.cancelMessage"),
                        languageConf.get("Confirmation.areYouSure"), languageConf);
        Optional<ButtonType> button = confirmation.showAndWait();
        if(button.isPresent() && button.get() == ButtonType.YES) {
            try {
                server.removeTransaction(transaction);
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }
        }
    }

    /**
     * Finds the maximum value from the map and returns the key
     *
     * @param debtMap map to be searched
     * @return the Participant key with the maximum value
     */
    public static Participant getMax(Map<Participant, Double> debtMap) {
        Participant result = null;
        for (Participant p : debtMap.keySet()) {
            if (result == null) result = p;
            else if (debtMap.get(p) > debtMap.get(result)) result = p;
        }
        return result;
    }

    /**
     * Finds the minimum value from the map and returns the key
     *
     * @param debtMap map to be searched
     * @return the Participant key with the minimum value
     */
    public static Participant getMin(Map<Participant, Double> debtMap) {
        Participant result = null;
        for (Participant p : debtMap.keySet()) {
            if (result == null) result = p;
            else if (debtMap.get(p) < debtMap.get(result)) result = p;
        }
        return result;
    }

    /**
     * Initializes the shortcuts for DebtsPage:
     *      Escape: go back
     * @param scene scene the listeners are initialised in
     */
    public void initializeShortcuts(Scene scene) {
        MainCtrl.checkKey(scene, this::backButtonClicked, KeyCode.ESCAPE);
    }
}
