package client.components;

import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.function.Consumer;


public class SettledDebtsListItem extends HBox {

    private final Transaction transaction;
    private final Consumer<Transaction> callBackCancel;
    @FXML
    private Label label;

    /**
     * @param transaction transaction this item represents
     * @param userConfig user config
     * @param languageConf language config
     * @param callBackCancel callback when cancel button is pressed
     * @param converter currency converter
     */
    public SettledDebtsListItem(Transaction transaction, UserConfig userConfig,
                                LanguageConf languageConf,
                                Consumer<Transaction> callBackCancel, CurrencyConverter converter) {
        this.transaction = transaction;
        this.callBackCancel = callBackCancel;
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass()
                        .getResource("/client/components/SettledDebtsListItem.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setResources(languageConf.getLanguageResources());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, languageConf.get("Component.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            return;
        }
        double convertedAmount;
        try {
            convertedAmount = converter.convert("USD", userConfig.getCurrency(),
                    transaction.getAmount(), transaction.getDate().toInstant());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("Currency.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            return;
        }
        NumberFormat formater = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formater.setMaximumFractionDigits(2);
        formater.setCurrency(Currency.getInstance(userConfig.getCurrency()));
        String formattedAmount = formater.format(convertedAmount);
        label.setText(String.format(languageConf.get("SettledDebtsListItem.label"),
                transaction.getGiver().getName(), transaction.getReceiver().getName(),
                formattedAmount));
    }

    /**
     * Cancel transaction
     */
    @FXML
    public void cancel() {
        callBackCancel.accept(transaction);
    }
}
