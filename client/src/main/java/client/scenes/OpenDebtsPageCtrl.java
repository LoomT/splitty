package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.ExpandedOpenDebtsListItem;
import client.components.ShrunkOpenDebtsListItem;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import commons.*;
import jakarta.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.*;

public class OpenDebtsPageCtrl {

    @FXML
    private PieChart shareChart = new PieChart();

    @FXML
    private Text totalSumExp;

    @FXML
    private VBox allDebtsPane;

    private final LanguageConf languageConf;
    private Event event;

    private final ServerUtils server;
    private final MainCtrlInterface mainCtrl;
    private final Websocket websocket;
    private Map<String, Double> participantDebtMap = new HashMap<>();


    /**
     * Constructor
     *
     * @param serverUtils  the server utils
     * @param mainCtrl     the main controller
     * @param languageConf language conf of the user
     * @param websocket    websocket
     */
    @Inject
    public OpenDebtsPageCtrl(
            ServerUtils serverUtils,
            MainCtrlInterface mainCtrl,
            LanguageConf languageConf,
            Websocket websocket) {
        this.server = serverUtils;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.websocket = websocket;
    }

    /**
     * Initialize the websockets for the OpenDebtsPageCtrl.
     */
    public void initialize() {
        websocket.on(WebsocketActions.ADD_TRANSACTION,
                transaction -> {
                    if (event.getTransactions().contains((Transaction) transaction)) {
                        return;
                    }
                    event.addTransaction((Transaction) transaction);
                    displayOpenDebtsPage(event);
                });

        websocket.on(WebsocketActions.REMOVE_TRANSACTION,
                id -> {
                    event.getTransactions().removeIf(t -> t.getId() == (Long) id);
                    displayOpenDebtsPage(event);
                });
    }

    /**
     * Displays the open debts page
     *
     * @param event the event
     */
    public void displayOpenDebtsPage(Event event) {

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
        if (event.getExpenses().isEmpty()) return;
        allDebtsPane.getChildren().clear();
        double sum = initializePage(map, partToPartMap);

        if (map.equals(participantDebtMap)) return;
        participantDebtMap = map;

        this.shareChart.getData().clear();
        for (String s : map.keySet()) {
            this.shareChart.getData().add(new PieChart.Data(s, map.get(s)));
        }

        for (PieChart.Data data : shareChart.getData()) {
            data.setName(data.getName() + ": " + data.getPieValue());
        }
        totalSumExp.setText("Total sum of all expenses in this event: " + sum);
    }

    /**
     * Initializes the graph and the open debts
     *
     * @param graphMap map to be used to populate the graph
     * @param debtMap  map to be used to populate the debts
     * @return sum of all the expenses
     */
    public double initializePage(Map<String, Double> graphMap,
                                 Map<Participant, Map<Participant, Double>> debtMap) {
        double sum = 0;
        for (Expense e : event.getExpenses()) {
            for (Participant p : e.getExpenseParticipants()) {
                double cost = e.getAmount() / e.getExpenseParticipants().size();
                graphMap.put(p.getName(), graphMap.get(p.getName()) + cost);
                if (e.getExpenseAuthor().equals(p)) continue;

                Map<Participant, Double> temp = debtMap.get(e.getExpenseAuthor());

                temp.put(p, temp.get(p) + cost);
            }
            sum += e.getAmount();
        }
        for (Participant receiver : debtMap.keySet()) {
            for (Participant giver : debtMap.get(receiver).keySet()) {
                double cost = debtMap.get(receiver).get(giver)
                        - event.getTransactions().stream().distinct().
                        filter(x -> x.getGiver().equals(giver) && x.getReceiver().
                                equals(receiver)).mapToDouble(Transaction::getAmount).sum();
                debtMap.get(receiver).put(giver, cost);
            }
        }
        minCashFlow(debtMap, event);
        return sum;
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

        allDebtsPane.getChildren().add(new ShrunkOpenDebtsListItem(maxDebit,
                maxCredit, min, event, languageConf, server, mainCtrl));
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
        if (index == -1) { //TODO
            //System.out.println("An error");
            return;
        }
        if (item.getClass() == ShrunkOpenDebtsListItem.class) {
            ShrunkOpenDebtsListItem oldItem = (ShrunkOpenDebtsListItem) list.get(index);
            list.set(index, new ExpandedOpenDebtsListItem(oldItem.getLender(),
                    oldItem.getDebtor(),
                    oldItem.getAmount(),
                    event,
                    languageConf,
                    server,
                    mainCtrl));
        } else {
            ExpandedOpenDebtsListItem oldItem = (ExpandedOpenDebtsListItem) list.get(index);
            allDebtsPane.getChildren().set(index, new ShrunkOpenDebtsListItem(oldItem.getLender(),
                    oldItem.getDebtor(),
                    oldItem.getAmount(),
                    event,
                    languageConf,
                    server,
                    mainCtrl));
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

}
