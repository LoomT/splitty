package client.scenes;

import client.components.DebtListItem;
import client.utils.ServerUtils;
import commons.Event;
import commons.Expense;
import commons.Participant;
import jakarta.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.*;

public class OpenDebtsPageCtrl {

    @FXML
    private Button backButton;

    @FXML
    private PieChart shareChart = new PieChart();

    @FXML
    private Text totalSumExp;

    @FXML
    private VBox debtList;

    private Event event;

    private ServerUtils server;
    private boolean idk = true;
    private MainCtrl mainCtrl;
    private Map<String, Double> participantDebtMap = new HashMap<>();


    /**
     * Constructor
     *
     * @param serverUtils the server utils
     * @param mainCtrl the main controller
     */
    @Inject
    public OpenDebtsPageCtrl(
            ServerUtils serverUtils,
            MainCtrl mainCtrl
    ) {
        this.server = serverUtils;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Displays the open debts page
     *
     * @param event the event
     */
    public void displayOpenDebtsPage(Event event) {
        this.event = event;
        Map<String, Double> map = new HashMap<>();
        event.getParticipants().forEach(x -> map.put(x.getName(), 0.0));
        double sum = 0;
        for(Expense e : event.getExpenses()){
            for(Participant p : e.getExpenseParticipants()){
                double cost = e.getAmount() / e.getExpenseParticipants().size();
                map.put(p.getName(), map.get(p.getName()) + cost);
            }
            sum += e.getAmount();
        }
        if(map.equals(participantDebtMap)) return;
        participantDebtMap = map;

        List<PieChart.Data> removalList = new ArrayList<>(this.shareChart.getData());
        this.shareChart.getData().removeAll(removalList);
        for(String s : map.keySet()){
            this.shareChart.getData().add(new PieChart.Data(s, map.get(s)));
        }

        addDebtsToDebtList();


        for (PieChart.Data data : shareChart.getData()) {
            data.setName(data.getName() + ": " + data.getPieValue());
        }
        totalSumExp.setText("Total sum of all expenses in this event: " + sum);

    }

    /**
     * Adds the debts to the debt list
     */
    private void addDebtsToDebtList() {
        List<DebtListItem> debtListItems = new ArrayList<>();
        debtList.getChildren().clear();
        for (String name : participantDebtMap.keySet()) {
            debtListItems.add(new DebtListItem(name + " owes: \n" + participantDebtMap.get(name)));
        }
        for(DebtListItem item : debtListItems){
            debtList.getChildren().add(item);
        }
    }


    /**
     * Handles the back button click event functionality
     */
    @FXML
    public void backButtonClicked() {
        mainCtrl.goBackToEventPage(event);
    }
}
