package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Tag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class StatisticsCtrl {

    @FXML
    private PieChart pc;

    @FXML
    private Text cost;

    @FXML
    private Button back;

    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final Websocket websocket;
    private final LanguageConf languageConf;

    private List<Tag> displayedTags = new ArrayList<>();

    PieChart.Data noTagSlice = null;
    /**
     * @param mainCtrl main control instance
     * @param server   server utils instance
     * @param websocket websocket client
     * @param languageConf language config
     */
    @Inject
    public StatisticsCtrl(
            MainCtrlInterface mainCtrl,
            ServerUtils server,
            Websocket websocket,
            LanguageConf languageConf
    ) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.websocket = websocket;
        this.languageConf = languageConf;
    }

    public void initialize() {
        //pc.getData().add(noTagSlice);
    }

    public void displayStatisticsPage(Event event) {
        //pc.getData().clear();
        initPieChart(event);
        initCost(event);
        back.setOnAction(e -> {
            mainCtrl.goBackToEventPage(event);
        });
    }

    public double initCost(Event event) {
        double totalCost = 0;
        //this does not take into account the currencies
        for (Expense exp : event.getExpenses()) {
            totalCost += exp.getAmount();
        }
        cost.setText("Total cost: " + totalCost);
        return totalCost;
    }

    public void initPieChart(Event event) {
        pc.setLegendVisible(false);
        double totalCost = initCost(event);
        for (Tag t : event.getTags()) {
            if (t != null) {
                double currCost = getAmount(event, t);
                double percentage = currCost / totalCost * 100;
                String formattedPercentage = String.format("%.2f", percentage);
                if (currCost > 0) {
                    if (!displayedTags.contains(t)) {
                        displayedTags.add(t);
                        String temp = "";
                        temp += t.getName() + "\n";
                        temp += formattedPercentage + "%";
                        temp += " (" + currCost + ")";

                        PieChart.Data slice = new PieChart.Data(temp, currCost);
                        pc.getData().add(slice);
                        if (slice.getNode() != null) {
                            Color color = hexToColor(t.getColor());
                            slice.getNode().setStyle("-fx-pie-color: #" + color.toString().substring(2));
                        }
                    }
                }
            }
//            else {
//                double costExpensesNoTag = 0;
//                for (Expense exp : event.getExpenses()) {
//                    if (exp.getType() == null) {
//                        costExpensesNoTag += exp.getAmount();
//                    }
//                }
//                double percentage = costExpensesNoTag / totalCost * 100;
//                String formattedPercentage = String.format("%.2f", percentage);
//                String temp = "";
//                temp += "No tag\n";
//                temp += formattedPercentage + "%";
//                temp += " (" + costExpensesNoTag + ")";
//
//                // Update the existing "No tag" slice
//                noTagSlice.setName(temp);
//                noTagSlice.setPieValue(costExpensesNoTag);
//
//                // Ensure "No tag" slice is white
//                if (noTagSlice.getNode() != null) {
//                    noTagSlice.getNode().setStyle("-fx-pie-color: #FFFFFF");
//                }
//            }
            boolean ok = false;
            for (PieChart.Data da : pc.getData()) {
                if (da.getName().contains("No tag")) {
                    ok = true;
                }
            }
            boolean gasit = false;
            for (Expense exp : event.getExpenses()) {
                if (exp.getType() == null) {
                    gasit = true;
                }
            }
            if (!ok) {
                if (gasit) {
                    double costExpensesNoTag = 0;
                    for (Expense exp : event.getExpenses()) {
                        if (exp.getType() == null) {
                            costExpensesNoTag += exp.getAmount();
                        }
                    }
                    double percentage = costExpensesNoTag / totalCost * 100;
                    String formattedPercentage = String.format("%.2f", percentage);
                    String temp = "";
                    temp += "No tag\n";
                    temp += formattedPercentage + "%";
                    temp += " (" + costExpensesNoTag + ")";
                    PieChart.Data slice = new PieChart.Data(temp, costExpensesNoTag);
                    pc.getData().add(slice);
                }
            } else {
                for (PieChart.Data da : pc.getData()) {
                    if (da.getName().contains("No tag")) {
                        double costExpensesNoTag = 0;
                        for (Expense exp : event.getExpenses()) {
                            if (exp.getType() == null) {
                                costExpensesNoTag += exp.getAmount();
                            }
                        }
                        double percentage = costExpensesNoTag / totalCost * 100;
                        String formattedPercentage = String.format("%.2f", percentage);
                        String temp = "";
                        temp += "No tag\n";
                        temp += formattedPercentage + "%";
                        temp += " (" + costExpensesNoTag + ")";
                        da.setName(temp);
                        da.setPieValue(costExpensesNoTag);
                    }
                }
            }
            for (Expense exp : event.getExpenses()) {
                if (exp.getType() == null) {

                }
            }
        }



//        for (PieChart.Data slice : pc.getData()) {
//            if (slice.getName().equals("No tag")) {
//                noTagSlice = slice;
//                break;
//            }
//        }
//
//        if (noTagSlice != null) {
//            double costExpensesNoTag = 0;
//            for (Expense exp : event.getExpenses()) {
//                if (exp.getType() == null) {
//                    costExpensesNoTag += exp.getAmount();
//                }
//            }
//            double percentage = costExpensesNoTag / totalCost * 100;
//            String formattedPercentage = String.format("%.2f", percentage);
//            String temp = "";
//            temp += "No tag\n";
//            temp += formattedPercentage + "%";
//            temp += " (" + costExpensesNoTag + ")";
//
//            // Update the existing "No tag" slice
//            noTagSlice.setName(temp);
//            noTagSlice.setPieValue(costExpensesNoTag);
//
//            // Ensure "No tag" slice is white
//            if (noTagSlice.getNode() != null) {
//                noTagSlice.getNode().setStyle("-fx-pie-color: #FFFFFF");
//            }
//        }

//        // Update the "No tag" slice value
//        double costExpensesNoTag = 0;
//        for (Expense exp : event.getExpenses()) {
//            if (exp.getType() == null) {
//                costExpensesNoTag += exp.getAmount();
//            }
//        }
//        double percentage = costExpensesNoTag / totalCost * 100;
//        String formattedPercentage = String.format("%.2f", percentage);
//        String temp = "";
//        temp += "No tag\n";
//        temp += formattedPercentage + "%";
//        temp += " (" + costExpensesNoTag + ")";
//
//        // Update the existing "No tag" slice
//        noTagSlice.setName(temp);
//        noTagSlice.setPieValue(costExpensesNoTag);
//
//        // Ensure "No tag" slice is white
//        if (noTagSlice.getNode() != null) {
//            noTagSlice.getNode().setStyle("-fx-pie-color: #FFFFFF");
//        }
    }



    public double getAmount(Event event, Tag tag) {
        double rez = 0;
        for (Expense exp : event.getExpenses()) {
            if (tag != null && exp.getType() != null) {
                if (exp.getType().equals(tag)) {
                    rez += exp.getAmount();
                }
            }
        }
        return rez;
    }

    public static Color hexToColor(String hexCode) {
        if (!hexCode.startsWith("#")) {
            hexCode = "#" + hexCode;
        }

        int red = Integer.parseInt(hexCode.substring(1, 3), 16);
        int green = Integer.parseInt(hexCode.substring(3, 5), 16);
        int blue = Integer.parseInt(hexCode.substring(5, 7), 16);

        return Color.rgb(red, green, blue);
    }
}
