package client.scenes;

import client.components.OpenDebtsListItem;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import commons.Event;
import commons.Expense;
import commons.Participant;
import jakarta.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    private VBox allDebtsPane;

    @FXML
    private ChoiceBox<String> includingChoiceBox;
    private final LanguageConf languageConf;
    private String selectedParticipantName;
    private Event event;
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private Map<String, Double> participantDebtMap = new HashMap<>();
    private Map<Map.Entry<Participant, Participant>, Double> participantToParticipantMap = new HashMap<>();


    /**
     * Constructor
     *
     * @param serverUtils the server utils
     * @param mainCtrl    the main controller
     */
    @Inject
    public OpenDebtsPageCtrl(
            ServerUtils serverUtils,
            MainCtrl mainCtrl,
            LanguageConf languageConf
    ) {
        this.server = serverUtils;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.selectedParticipantName = languageConf.get("OpenDebtsPage.allParticipants");
    }

    /**
     * Displays the open debts page
     *
     * @param event the event
     */
    public void displayOpenDebtsPage(Event event) {
        this.event = event;
        Map<String, Double> map = new HashMap<>();
        Map<Map.Entry<Participant, Participant>, Double> debtMap = new HashMap<>();
        event.getParticipants().forEach(x -> map.put(x.getName(), 0.0));
        double sum = 0;

        includingChoiceBox.getItems().clear();
        includingChoiceBox.setValue(languageConf.get("OpenDebtsPage.allParticipants"));
        includingChoiceBox.getItems().add(languageConf.get("OpenDebtsPage.allParticipants"));
        includingChoiceBox.getItems().addAll(
                event.getParticipants().stream().map(Participant::getName).toList()
        );
        selectedParticipantName = languageConf.get("OpenDebtsPage.allParticipants");

        for (Expense e : event.getExpenses()) {
            for (Participant p : e.getExpenseParticipants()) {
                double cost = e.getAmount() / e.getExpenseParticipants().size();
                map.put(p.getName(), map.get(p.getName()) + cost);
                if(!e.getExpenseAuthor().equals(p)) debtMap.put(Map.entry(e.getExpenseAuthor(), p), cost);
            }
            sum += e.getAmount();
        }

        participantToParticipantMap = debtMap;
        populateExpense(selectedParticipantName);

        includingChoiceBox.setOnAction(e -> {
            if(includingChoiceBox.getSelectionModel().getSelectedItem() != null)
                selectedParticipantName = includingChoiceBox.getSelectionModel().getSelectedItem();
            populateExpense(selectedParticipantName);
        });

        if (map.equals(participantDebtMap)) return;
        participantDebtMap = map;

        this.shareChart.getData().clear();
        for (String s : map.keySet()) {
            this.shareChart.getData().add(new PieChart.Data(s, map.get(s)));
        }

        for (PieChart.Data data : shareChart.getData()) {
            data.setName(data.getName() + ": " + (int) data.getPieValue() + "%");
        }
        totalSumExp.setText("Total sum of all expenses in this event: " + sum);
    }

    public void populateExpense(String name) {
        Participant participant = null;
        boolean flag = false;
        for (Participant p : event.getParticipants()) {
            if (p.getName().equals(name)) {
                flag = true;
                participant = p;
                break;
            }
        }
        allDebtsPane.getChildren().clear();
        for (Map.Entry<Participant, Participant> m : participantToParticipantMap.keySet()) {
            if (!flag || m.getKey().equals(participant) || m.getValue().equals(participant)) {
                allDebtsPane.getChildren().add(new OpenDebtsListItem(
                        m.getKey(), m.getValue(), participantToParticipantMap.get(m)));
            }
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
