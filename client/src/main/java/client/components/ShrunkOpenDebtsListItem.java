package client.components;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.currency.CurrencyConverter;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.ConnectException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.function.Consumer;

public class ShrunkOpenDebtsListItem extends HBox {
    @FXML
    private Label participantLabel;
    private final Transaction transaction;
    private final Consumer<ShrunkOpenDebtsListItem> callBackExpand;
    private final Consumer<Transaction> callBackSettle;

    /**
     * Constructor for ShrunkOpenDebtsListItem
     *
     * @param transaction pre-made transaction
     * @param languageConf languageConf of the page
     * @param callBackExpand shrink when this component is clicked
     * @param callBackSettle settle when clicked
     * @param converter currency converter
     * @param mainCtrl main controller
     */
    public ShrunkOpenDebtsListItem(Transaction transaction,
                                   LanguageConf languageConf,
                                   Consumer<ShrunkOpenDebtsListItem> callBackExpand,
                                   Consumer<Transaction> callBackSettle,
                                   CurrencyConverter converter,
                                   MainCtrlInterface mainCtrl) {
        this.transaction = transaction;
        this.callBackExpand = callBackExpand;
        this.callBackSettle = callBackSettle;
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/client/components/OpenDebtsListItem.fxml")
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
            convertedAmount = converter.convert("USD", transaction.getCurrency(),
                    transaction.getAmount(), transaction.getDate().toInstant());
        } catch (CurrencyConverter.CurrencyConversionException e) {
            return;
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        String template = languageConf.get("OpenDebtsListItem.template");
        NumberFormat formater = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formater.setMaximumFractionDigits(2);
        formater.setCurrency(Currency.getInstance(transaction.getCurrency()));
        String formattedAmount = formater.format(convertedAmount);
        String text = String.format(template, transaction.getGiver().getName(),
                transaction.getReceiver().getName(), formattedAmount);
        participantLabel.setText(text);
    }

    /**
     * expands the item to show BankAccount information
     */
    public void onEventClicked(){
        callBackExpand.accept(this);
    }


    /**
     * Settles the debt displayed in the item
     */
    public void settleDebt(){
        callBackSettle.accept(transaction);
    }

    /**
     * @return pre-made transaction
     */
    public Transaction getTransaction() {
        return transaction;
    }
}
