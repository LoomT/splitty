package client.scenes;

import client.utils.ServerUtils;
import commons.Event;
import commons.Expense;
import commons.Participant;
import jakarta.inject.Inject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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
    private ScrollPane allDebtsPane;

    private Event event;
    private ServerUtils server;
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

        for (PieChart.Data data : shareChart.getData()) {
            data.setName(data.getName() + ": " + (int) data.getPieValue() + "%");
        }
        totalSumExp.setText("Total sum of all expenses in this event: " + sum);

    }


    /**
     * Handles the back button click event functionality
     */
    @FXML
    public void backButtonClicked() {
        mainCtrl.goBackToEventPage(event);
    }
}
