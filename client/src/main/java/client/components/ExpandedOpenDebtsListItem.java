package client.components;

import client.utils.LanguageConf;
import client.utils.currency.CurrencyConverter;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.function.Consumer;

public class ExpandedOpenDebtsListItem extends HBox {
    @FXML
    private Label participantLabel;
    @FXML
    private Text availability;
    @FXML
    private VBox detailContainer;
    private final Transaction transaction;
    private final Consumer<ExpandedOpenDebtsListItem> callBackShrink;
    private final Consumer<Transaction> callBackSettle;


    /**
     * Constructor for ExpandedOpenDebtsListItem
     *
     * @param transaction pre-made transaction
     * @param languageConf languageConf of the page
     * @param callBackShrink shrink when this component is clicked
     * @param callBackSettle settle when clicked
     * @param converter currency converter
     */
    public ExpandedOpenDebtsListItem(Transaction transaction,
                                     LanguageConf languageConf,
                                     Consumer<ExpandedOpenDebtsListItem> callBackShrink,
                                     Consumer<Transaction> callBackSettle,
                                     CurrencyConverter converter) {
        this.transaction = transaction;
        this.callBackShrink = callBackShrink;
        this.callBackSettle = callBackSettle;
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/client/components/ExpandedOpenDebtsListItem.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setResources(languageConf.getLanguageResources());
        try {
            fxmlLoader.load();
            initializeFields(languageConf);
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
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("Currency.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            alert.showAndWait();
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
     * contracts the item
     */
    public void onEventClicked() {
        callBackShrink.accept(this);
    }

    /**
     * initializes the expanded Bank account information fields
     *
     * @param languageConf languageConf of the page
     */
    public void initializeFields(LanguageConf languageConf) {
        if ((transaction.getReceiver().getBeneficiary() == null
                || transaction.getReceiver().getAccountNumber() == null)
                || (transaction.getReceiver().getAccountNumber().isEmpty()
                || transaction.getReceiver().getBeneficiary().isEmpty())) {
            availability.setText(languageConf.get("ExpandedOpenDebtsListItem.bankAccountEmpty"));
        } else {
            availability.setText(languageConf.get(
                    "ExpandedOpenDebtsListItem.bankAccountFull"));
            Label name = new Label(String.format(
                    languageConf.get("ExpandedOpenDebtsListItem.beneficiary"),
                    transaction.getReceiver().getBeneficiary()));
            name.getStyleClass().add("textFont");
            detailContainer.getChildren().add(name);
            Label iban = new Label(String.format(
                    languageConf.get("ExpandedOpenDebtsListItem.iban"),
                    transaction.getReceiver().getAccountNumber()));
            iban.getStyleClass().add("textFont");
            detailContainer.getChildren().add(iban);
            if(transaction.getReceiver().getBic() != null
                    && !transaction.getReceiver().getBic().isEmpty()) {
                Label label = new Label(String.format(
                        languageConf.get("ExpandedOpenDebtsListItem.bic"),
                        transaction.getReceiver().getBic()));
                label.getStyleClass().add("textFont");
                detailContainer.getChildren().add(label);
            }
        }
        Label label;
        if(transaction.getReceiver().getEmailAddress() == null
                || transaction.getReceiver().getEmailAddress().isEmpty()) {
            label = new Label(languageConf.get("ExpandedOpenDebtsListItem.emailUnavailable"));
        } else {
            label = new Label(String.format(
                    languageConf.get("ExpandedOpenDebtsListItem.emailAvailable"),
                    transaction.getReceiver().getEmailAddress()));
        }
        label.getStyleClass().add("textFont");
        detailContainer.getChildren().add(label);
    }

    /**
     * Settles the debt displayed in the item
     */
    public void settleDebt() {
        callBackSettle.accept(transaction);
    }

    /**
     * @return pre-made transaction
     */
    public Transaction getTransaction() {
        return transaction;
    }
}