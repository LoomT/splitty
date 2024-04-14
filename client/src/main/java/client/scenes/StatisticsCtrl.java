package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import client.utils.currency.CurrencyConverter;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.ConnectException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import static commons.WebsocketActions.*;

public class StatisticsCtrl {

    @FXML
    private PieChart pc;

    @FXML
    private Text cost;

    @FXML
    private Button back;
    @FXML
    private VBox legend;

    private Event event;

    private final MainCtrlInterface mainCtrl;
    private final Websocket websocket;
    private final LanguageConf languageConf;
    private final CurrencyConverter converter;
    private final UserConfig userConfig;
    private boolean opened;

    /**
     * @param mainCtrl main control instance
     * @param websocket websocket client
     * @param languageConf language config
     * @param converter currency converter
     * @param userConfig user config
     */
    @Inject
    public StatisticsCtrl(
            MainCtrlInterface mainCtrl,
            Websocket websocket,
            LanguageConf languageConf,
            CurrencyConverter converter,
            UserConfig userConfig
    ) {
        this.mainCtrl = mainCtrl;
        this.websocket = websocket;
        this.languageConf = languageConf;
        this.converter = converter;
        this.userConfig = userConfig;
        opened = false;
    }

    /**
     * initialize method
     */
    public void initialize() {
        websocket.on(ADD_TAG, tag -> {
            populateLegend(event);
        });
        back.setOnAction(e -> {
            handleBackButton(event);
        });
        websocket.on(ADD_EXPENSE, e -> {
            initPieChart(event);
        });
        websocket.on(UPDATE_EXPENSE, e -> {
            initPieChart(event);
        });
        websocket.on(REMOVE_EXPENSE, e -> {
            initPieChart(event);
        });
    }

    /**
     * populate the legend of the pie-chart
     * @param event the current event
     */
    private void populateLegend(Event event) {
        if(!opened) return;
        legend.getChildren().clear();

        for (Tag tag : event.getTags()) {
            String tagName = tag.getName();
            if (!tagName.equals("No tag")) {
                Label label = new Label(tagName);
                label.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

                Shape coloredBox = new Rectangle(10, 10);
                coloredBox.setFill(Color.web(tag.getColor()));

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
        this.event = event;
        opened = true;
        pc.setLegendVisible(false);
        initPieChart(event);
    }

    /**
     * method for handling the back button action
     * @param event
     */
    public void handleBackButton(Event event) {
        pc.getData().clear();
        opened = false;
        mainCtrl.goBackToEventPage(event);
    }

    /**
     * initialize the total cost of the event
     * @param event the current event
     * @return the total cost
     */
    public double initCost(Event event) {
        double totalCost = 0;
        for (Expense exp : event.getExpenses()) {
            double amount = exp.getAmount();
            try {
                if(!userConfig.getCurrency().equals("NONE")) {
                    amount = converter.convert("USD", userConfig.getCurrency(),
                            amount, exp.getDate().toInstant());
                    totalCost += amount;
                }

            } catch (CurrencyConverter.CurrencyConversionException ignored) {
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }
        }
        String preferedCurrency = userConfig.getCurrency();
        String form = getCurrencySymbol(totalCost, preferedCurrency);
        cost.setText(languageConf.get("Statistics.totalCost") + form);
        return totalCost;
    }

    /**
     * initialize the piechart
     * @param event the current event
     */
    public void initPieChart(Event event) {
        if(!opened) return;
        double totalCost = initCost(event);
        updateTagsPieChart(event, totalCost);
        updateNoTagPieChart(event, totalCost);
        populateLegend(event);
    }

    /**
     * see which tags have associated expenses
     * @param event the current event
     * @param totalCost the total cost of the event
     */
    private void updateTagsPieChart(Event event, double totalCost) {
        for (Tag tag : event.getTags()) {
            if (tag != null) {
                double currCost = getAmount(event, tag);
                if (currCost > 0) {
                    updateOrAddTagSlice(tag, currCost, totalCost);
                } else {
                    pc.getData().removeIf(slice -> slice.getName().startsWith(tag.getName()));
                }
            }
        }
    }

    /**
     * update or add new slices
     * @param tag the current tag
     * @param currCost the current cost
     * @param totalCost the total cost of the event
     */
    private void updateOrAddTagSlice(Tag tag, double currCost, double totalCost) {
        double percentage = currCost / totalCost * 100;
        String preferedCurrency = userConfig.getCurrency();
        String form = getCurrencySymbol(currCost, preferedCurrency);
        String formattedPercentage = String.format("%.2f", percentage);
        String tagInfo = tag.getName() + "\n" + formattedPercentage + "% (" + form + ")";

        boolean found = false;
        for (PieChart.Data slice : pc.getData()) {
            if (slice.getName().startsWith(tag.getName())) {
                slice.setName(tagInfo);
                slice.setPieValue(currCost);
                found = true;
                break;
            }
        }

        if (!found) {
            PieChart.Data newSlice = new PieChart.Data(tagInfo, currCost);
            pc.getData().add(newSlice);
            applyTagColor(newSlice, tag.getColor());
        }
    }

    /**
     * apply color to tag
     * @param slice the slice
     * @param colorHex the color string
     */
    private void applyTagColor(PieChart.Data slice, String colorHex) {
        if (slice.getNode() != null) {
            Color color = hexToColor(colorHex);
            slice.getNode().setStyle("-fx-pie-color: #" + color.toString().substring(2));
        }
    }

    /**
     * update the no tag slice
     * @param event the current event
     * @param totalCost the total cost of the event
     */
    private void updateNoTagPieChart(Event event, double totalCost) {
        boolean hasNoTagSlice = pc.getData().stream().anyMatch(slice ->
                slice.getName().contains("No tag"));
        if (!hasNoTagSlice && containsExpensesWithNoTag(event)) {
            double costExpensesNoTag = calculateExpensesNoTag(event);
            String text = configureNoTag(costExpensesNoTag, totalCost);
            PieChart.Data slice = new PieChart.Data(text, costExpensesNoTag);
            pc.getData().add(slice);
            slice.getNode().setStyle("-fx-pie-color: #FFFFFF");
        } else {
            updateNoTagSlice(event, totalCost);
        }
    }

    /**
     * checks if there are expenses with no tags
     * @param event the current event
     * @return true if they are, false otherwise
     */
    private boolean containsExpensesWithNoTag(Event event) {
        return event.getExpenses().stream().anyMatch(expense -> expense.getType() == null);
    }

    /**
     * calculate the cost of the no tag expenses
     * @param event the current event
     * @return the amount
     */
    private double calculateExpensesNoTag(Event event) {
        double costExpensesNoTag = 0;
        for (Expense expense : event.getExpenses()) {
            if (expense.getType() == null) {
                double amount = expense.getAmount();
                try {
                    amount = converter.convert("USD", userConfig.getCurrency(),
                            amount, expense.getDate().toInstant());
                    costExpensesNoTag += amount;
                } catch (CurrencyConverter.CurrencyConversionException ignored) {
                } catch (ConnectException e) {
                    mainCtrl.handleServerNotFound();
                }
            }
        }
        return costExpensesNoTag;
    }

    /**
     * update the no tag slices
     * @param event the current event
     * @param totalCost the total cost of the event
     */
    private void updateNoTagSlice(Event event, double totalCost) {
        double costExpensesNoTag = calculateExpensesNoTag(event);
        if(costExpensesNoTag > 0)
            for (PieChart.Data slice : pc.getData()) {
                if (slice.getName().contains("No tag")) {
                    String text = configureNoTag(costExpensesNoTag, totalCost);
                    slice.setName(text);
                    slice.setPieValue(costExpensesNoTag);
                    break;
                }
            }
        else pc.getData().removeIf(slice -> slice.getName().contains("No tag"));
    }

    /**
     * configure the no tag slice
     * @param costExpensesNoTag cost of untagged expenses
     * @param totalCost total event cost
     * @return the text for no tag
     */
    public String configureNoTag(double costExpensesNoTag, double totalCost) {
        double percentage = costExpensesNoTag / totalCost * 100;
        String preferedCurrency = userConfig.getCurrency();
        String form = getCurrencySymbol(costExpensesNoTag, preferedCurrency);
        String formattedPercentage = String.format("%.2f", percentage);
        String temp = "";
        temp += "No tag\n";
        temp += formattedPercentage + "%";
        temp += " (" + form + ")";
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
            double amount = exp.getAmount();
            try {
                if(!userConfig.getCurrency().equals("NONE")) {
                    amount = converter.convert("USD", userConfig.getCurrency(),
                            amount, exp.getDate().toInstant());
                }

            } catch (CurrencyConverter.CurrencyConversionException ignored) {
            } catch (ConnectException e) {
                mainCtrl.handleServerNotFound();
            }

            if (tag != null && exp.getType() != null) {
                if (exp.getType().equals(tag)) {
                    rez += amount;
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
        if (hexCode == null || hexCode.isEmpty() || hexCode.equals("0x")) {
            return Color.BLACK;
        }
        hexCode = hexCode.replace("#", "").replace("0x", "");
        if (!hexCode.matches("[0-9a-fA-F]+")) {
            return Color.BLACK;
        }
        try {
            int red = Integer.parseInt(hexCode.substring(0, 2), 16);
            int green = Integer.parseInt(hexCode.substring(2, 4), 16);
            int blue = Integer.parseInt(hexCode.substring(4, 6), 16);
            return Color.rgb(red, green, blue);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return Color.BLACK;
        }
    }


    /**
     * handle the currency error
     * @param e the exception
     */
    private void handleCurrencyError(Exception e) {
        if (e instanceof ConnectException) {
            mainCtrl.handleServerNotFound();
        } else if (e instanceof IOException) {
            Alert alert = new Alert(Alert.AlertType.ERROR, languageConf.get("Currency.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            alert.showAndWait();
        }
    }

    /**
     * get the formatted amount plus the symbol
     * @param amount the amount
     * @param currency the currency
     * @return the formatted string
     */
    public String getCurrencySymbol(double amount, String currency) {
        NumberFormat formater = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formater.setMaximumFractionDigits(2);
        formater.setCurrency(Currency.getInstance(currency));
        return formater.format(amount);
    }

    /**
     * set the escape shortcut
     * @param scene
     */
    public void initializeShortcuts(Scene scene) {
        MainCtrl.checkKey(scene, () -> handleBackButton(event), KeyCode.ESCAPE);
    }
}