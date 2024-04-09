package client.scenes;

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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;

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
    private Event event;
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private Map<String, Double> participantDebtMap = new HashMap<>();
    private Map<Participant, Map<Participant, Double>>
            partToPartMap = new HashMap<>();
    private Websocket websocket;


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
            MainCtrl mainCtrl,
            LanguageConf languageConf,
            Websocket websocket
    ) {
        this.server = serverUtils;
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.websocket = websocket;
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
        double sum = calculateParticipants(map, partToPartMap);

        includingChoiceBox.getItems().clear();
        includingChoiceBox.getItems().add(languageConf.get("OpenDebtsPage.allParticipants"));
        includingChoiceBox.getItems().addAll(
                event.getParticipants().stream().map(Participant::getName).toList()
        );
        includingChoiceBox.setValue(languageConf.get("OpenDebtsPage.allParticipants"));
        includingChoiceBox.setOnAction(e -> {
            if (includingChoiceBox.getSelectionModel().getSelectedItem() == null)
                return;
            if(includingChoiceBox.getSelectionModel().getSelectedIndex() == 0)
                populateExpense(null);
            else
                populateExpense(includingChoiceBox.getSelectionModel().getSelectedItem());
        });
        populateExpense(null);

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

    public double calculateParticipants(Map<String, Double> map,
                                      Map<Participant, Map<Participant, Double>> debtMap){
        double sum = 0;
        for (Expense e : event.getExpenses()) {
            for (Participant p : e.getExpenseParticipants()) {
                double cost = e.getAmount() / e.getExpenseParticipants().size();
                map.put(p.getName(), map.get(p.getName()) + cost);
                if (e.getExpenseAuthor().equals(p)) continue;

                debtMap.putIfAbsent(e.getExpenseAuthor(), new HashMap<>());
                Map<Participant, Double> temp = debtMap.get(e.getExpenseAuthor());
                temp.putIfAbsent(p, 0.0);
                temp.put(p, temp.get(p) + cost);
            }
            sum += e.getAmount();
        }
        return sum;
    }


    public void populateExpense(String name) {
        Participant participant = null;
        if(name != null){
            participant = event.getParticipants().stream().filter(
                    p -> p.getName().equals(name)).toList().getFirst();
        }

        allDebtsPane.getChildren().clear();
        for (Participant receiver : partToPartMap.keySet()) {
            for(Participant giver : partToPartMap.get(receiver).keySet()){
                if((giver.equals(participant)
                        || receiver.equals(participant))){
                    System.out.println("test");
                    continue;
                }
                double cost = partToPartMap.get(receiver).get(giver) - event.getTransactions().stream()
                        .distinct().filter(x -> x.getGiver().equals(giver) && x.getReceiver().equals(receiver))
                        .mapToDouble(Transaction::getAmount).sum() + event.getTransactions().stream()
                        .distinct().filter(x -> x.getGiver().equals(receiver) && x.getReceiver().equals(giver))
                        .mapToDouble(Transaction::getAmount).sum();

                if (cost <= 0
                        || (giver.equals(participant)
                        && receiver.equals(participant))) {
                    continue;
                }

                if(partToPartMap.get(giver) == null || partToPartMap.get(giver).get(receiver) == null){
                    allDebtsPane.getChildren().add(new ShrunkOpenDebtsListItem(receiver,
                            giver, cost, event, languageConf, server, mainCtrl));
                }
                else if(cost - partToPartMap.get(giver).get(receiver) > 0){
                    cost -= partToPartMap.get(giver).get(receiver);
                    allDebtsPane.getChildren().add(new ShrunkOpenDebtsListItem(receiver,
                            giver, cost, event, languageConf, server, mainCtrl));
                }

            }
        }
    }

    public void resizeOpenDebtItem(Node item){
        int index = -1;
        List<Node> list = allDebtsPane.getChildren();
        for(int i = 0; i<list.size(); i++){
            if(item.equals(list.get(i))){
                index = i;
            }
        }
        if(index == -1){ //TODO
            //System.out.println("An error");
            return;
        }
        if(item.getClass() == ShrunkOpenDebtsListItem.class){
            ShrunkOpenDebtsListItem oldItem = (ShrunkOpenDebtsListItem) list.get(index);
            list.set(index, new ExpandedOpenDebtsListItem(oldItem.getLender(),
                    oldItem.getDebtor(),
                    oldItem.getAmount(),
                    event,
                    languageConf,
                    server,
                    mainCtrl));
        }
        else{
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
}
