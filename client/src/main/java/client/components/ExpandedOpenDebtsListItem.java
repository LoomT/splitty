package client.components;

import client.MockClass.MainCtrlInterface;
import client.utils.EmailService;
import client.utils.LanguageConf;
import client.utils.currency.CurrencyConverter;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.ConnectException;
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
    private final EmailService emailService;
    private final LanguageConf languageConf;
    private final CurrencyConverter converter;
    private int size;


    /**
     * Constructor for ExpandedOpenDebtsListItem
     *
     * @param transaction    pre-made transaction
     * @param languageConf   languageConf of the page
     * @param callBackShrink shrink when this component is clicked
     * @param callBackSettle settle when clicked
     * @param converter      currency converter
     * @param mainCtrl       main controller
     */
    public ExpandedOpenDebtsListItem(Transaction transaction,
                                     LanguageConf languageConf,
                                     Consumer<ExpandedOpenDebtsListItem> callBackShrink,
                                     Consumer<Transaction> callBackSettle,
                                     CurrencyConverter converter,
                                     MainCtrlInterface mainCtrl,
                                     EmailService emailService) {
        this.transaction = transaction;
        this.callBackShrink = callBackShrink;
        this.callBackSettle = callBackSettle;
        this.emailService = emailService;
        this.languageConf = languageConf;
        this.converter = converter;
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
        } catch (CurrencyConverter.CurrencyConversionException e) {
            return;
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        String template = languageConf.get("OpenDebtsListItem.template");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(2);
        formatter.setCurrency(Currency.getInstance(transaction.getCurrency()));
        String formattedAmount = formatter.format(convertedAmount);
        String text = String.format(template, transaction.getGiver().getName(),
                transaction.getReceiver().getName(), formattedAmount);
        participantLabel.setText(text);
        size = detailContainer.getChildren().size();
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
            if (transaction.getReceiver().getBic() != null
                    && !transaction.getReceiver().getBic().isEmpty()) {
                Label label = new Label(String.format(
                        languageConf.get("ExpandedOpenDebtsListItem.bic"),
                        transaction.getReceiver().getBic()));
                label.getStyleClass().add("textFont");
                detailContainer.getChildren().add(label);
            }
        }
        Label label;
        if (transaction.getReceiver().getEmailAddress() == null
                || transaction.getReceiver().getEmailAddress().isEmpty()) {
            label = new Label(languageConf.get("ExpandedOpenDebtsListItem.emailUnavailable"));
            label.getStyleClass().add("textFont");
            detailContainer.getChildren().add(label);
        } else {
            label = new Label(String.format(
                    languageConf.get("ExpandedOpenDebtsListItem.emailAvailable"),
                    transaction.getReceiver().getEmailAddress()));
            label.getStyleClass().add("textFont");
            Button button = new Button(languageConf.get("ExpandedOpenDebtsListItem.emailButton"));
            button.getStyleClass().add("pbutton");
            button.setOnAction(e -> sendEmail());
            detailContainer.getChildren().add(label);
            detailContainer.getChildren().add(button);
        }
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

    public void sendEmail() {
        String subject = languageConf.get("EmailService.reminderHeader");
        String body = languageConf.get("EmailService.reminderBody");

        double convertedAmount;
        try {
            convertedAmount = converter.convert("USD", transaction.getCurrency(),
                    transaction.getAmount(), transaction.getDate().toInstant());
        } catch (CurrencyConverter.CurrencyConversionException | ConnectException e) {
            throw new RuntimeException(e);
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(2);
        formatter.setCurrency(Currency.getInstance(transaction.getCurrency()));
        String formattedAmount = formatter.format(convertedAmount);
        body = String.format(body, formattedAmount,
                transaction.getReceiver().getName());

        boolean status = emailService.sendEmail(transaction.getReceiver().getEmailAddress(),
                subject, body);
        Label label;
        if(status){
            System.out.println("Email successful");
            label = new Label(languageConf.get("ExpandedOpenDebtsListItem.reminderSuccessful"));
            label.getStyleClass().add("textFont");
        } else{
            System.out.println("Email couldn't be sent");
            label = new Label(languageConf.get("ExpandedOpenDebtsListItem.reminderFailed"));
            label.getStyleClass().add("textFont");
        }
        if(detailContainer.getChildren().size() > size)
            detailContainer.getChildren().removeLast();
        detailContainer.getChildren().add(size, label);
    }
}