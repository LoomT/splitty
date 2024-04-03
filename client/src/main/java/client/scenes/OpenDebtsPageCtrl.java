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
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for(Participant participant : event.getParticipants()) {
            double sum = 0;
            for(Expense expense : event.getExpenses()) {
                for(Participant expenseParticipant : expense.getExpenseParticipants()) {
                    if(expenseParticipant.getName().equals(participant.getName())) {
                        sum += expense.getAmount() / expense.getExpenseParticipants().size();
                    }
                }
            }
            pieChartData.add(new PieChart.Data(participant.getName(), sum));
        }
        for (PieChart.Data data : shareChart.getData()) {
            data.setName(data.getName() + ": " + (int) data.getPieValue() + "%");
        }

        Font font = new Font("Arial", 12);
        for (PieChart.Data data : shareChart.getData()) {
            data.getNode().setStyle("-fx-font-size: " + font.getSize() + "px;");
        }
        this.shareChart.setData(pieChartData);
        this.shareChart.setStyle("-fx-background-color: transparent;");
        double totalSum = 0;
        for(Expense expense : event.getExpenses()) {
            totalSum += expense.getAmount();
        }
        totalSumExp.setText("Total sum of all expenses in this event: " + totalSum);
    }


    /**
     * Handles the back button click event functionality
     */
    @FXML
    public void backButtonClicked() {
        mainCtrl.goBackToEventPage(event);
    }
}
