package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
    @FXML
    private VBox legend;

    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final Websocket websocket;
    private final LanguageConf languageConf;

    private List<Tag> displayedTags = new ArrayList<>();

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

    /**
     * initialize method
     */
    public void initialize() {
    }

    private void populateLegend() {
        legend.getChildren().clear();

        for (PieChart.Data data : pc.getData()) {
            String[] lines = data.getName().split("\n");
            String tagName = lines[0];
            if (!tagName.equals("No tag")) {
                Label label = new Label(tagName);
                label.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

                Shape coloredBox = new Rectangle(10, 10);
                coloredBox.setFill(Color.web(data.getNode().getStyle().split(": ")[1]));

                HBox legendItem = new HBox(10);
                legendItem.getChildren().addAll(coloredBox, label);

                legend.getChildren().add(legendItem);
            }
        }
    }

    /**
     * display the statistics page
     * @param event the current event
     */
    public void displayStatisticsPage(Event event) {
        //pc.getData().clear();
        pc.setLegendVisible(false);
        initPieChart(event);
        initCost(event);
        back.setOnAction(e -> {
            mainCtrl.goBackToEventPage(event);
        });
    }

    /**
     * initialize the total cost of the event
     * @param event the current event
     * @return the total cost
     */
    public double initCost(Event event) {
        double totalCost = 0;
        //this does not take into account the currencies
        for (Expense exp : event.getExpenses()) {
            totalCost += exp.getAmount();
        }
        cost.setText("Total cost: " + totalCost);
        return totalCost;
    }

    /**
     * initialize the piechart
     * @param event the current event
     */
    public void initPieChart(Event event) {
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
                            slice.getNode().setStyle("-fx-pie-color: #" +
                                    color.toString().substring(2));
                        }
                    }
                }
            }
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
                    String text = configureNoTag(costExpensesNoTag, totalCost);
                    PieChart.Data slice = new PieChart.Data(text, costExpensesNoTag);
                    pc.getData().add(slice);
                    if (slice.getNode() != null) {
                        slice.getNode().setStyle("-fx-pie-color: #FFFFFF");
                    }
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
                        String text = configureNoTag(costExpensesNoTag, totalCost);
                        da.setName(text);
                        da.setPieValue(costExpensesNoTag);
                    }
                }
            }
            for (Expense exp : event.getExpenses()) {
                if (exp.getType() == null) {

                }
            }

        }
        populateLegend();
    }

    /**
     * configure the no tag slice
     * @param costExpensesNoTag
     * @param totalCost
     * @return the text for no tag
     */
    public String configureNoTag(double costExpensesNoTag, double totalCost) {
        double percentage = costExpensesNoTag / totalCost * 100;
        String formattedPercentage = String.format("%.2f", percentage);
        String temp = "";
        temp += "No tag\n";
        temp += formattedPercentage + "%";
        temp += " (" + costExpensesNoTag + ")";
        return temp;
    }

    /**
     * get the total amount for a tag in the event
     * @param event the current event
     * @param tag the current tag
     * @return the amount
     */
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

    /**
     * convert from string to Color
     * @param hexCode the string hexcode
     * @return the Color variable
     */
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
