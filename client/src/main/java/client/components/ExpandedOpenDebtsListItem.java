package client.components;

import client.MockClass.MainCtrlInterface;
import client.utils.EmailService;
import client.utils.LanguageConf;
import client.utils.currency.CurrencyConverter;
import commons.Event;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
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
    private final MainCtrlInterface mainCtrl;
    private final Event event;
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
     * @param emailService   email service
     * @param event          event
     */
    public ExpandedOpenDebtsListItem(Transaction transaction,
                                     LanguageConf languageConf,
                                     Consumer<ExpandedOpenDebtsListItem> callBackShrink,
                                     Consumer<Transaction> callBackSettle,
                                     CurrencyConverter converter,
                                     MainCtrlInterface mainCtrl,
                                     EmailService emailService,
                                     Event event) {
        this.transaction = transaction;
        this.callBackShrink = callBackShrink;
        this.callBackSettle = callBackSettle;
        this.emailService = emailService;
        this.languageConf = languageConf;
        this.converter = converter;
        this.mainCtrl = mainCtrl;
        this.event = event;
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
            Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            return;
        }
        double convertedAmount;
        try {
            convertedAmount = converter.convert("EUR", transaction.getCurrency(),
                    transaction.getAmount().doubleValue(), transaction.getDate().toInstant());
        } catch (CurrencyConverter.CurrencyConversionException e) {
            return;
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
            return;
        }
        String template = languageConf.get("OpenDebtsListItem.template");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(Currency.getInstance(transaction.getCurrency())
                .getDefaultFractionDigits());
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
        if (transaction.getGiver().getEmailAddress() == null
                || transaction.getGiver().getEmailAddress().isEmpty()) {
            label = new Label(languageConf.get("ExpandedOpenDebtsListItem.emailUnavailable"));
            label.getStyleClass().add("textFont");
            detailContainer.getChildren().add(label);
        } else {
            HBox hbox = new HBox();
            label = new Label(String.format(
                    languageConf.get("ExpandedOpenDebtsListItem.emailAvailable"),
                    transaction.getGiver().getEmailAddress()));
            label.getStyleClass().add("textFont");
            Button button = new Button(languageConf.get("ExpandedOpenDebtsListItem.emailButton"));
            button.getStyleClass().add("pbutton");
            button.setOnAction(e -> sendEmail());
            button.setMinWidth(USE_PREF_SIZE);
            hbox.getChildren().add(label);
            hbox.getChildren().add(button);
            hbox.setSpacing(10);
            hbox.setAlignment(Pos.CENTER_LEFT);
            detailContainer.getChildren().add(hbox);
            button.setDisable(emailService.isNotInitialized());
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

    /**
     * sends email reminder to the debtor
     */
    public void sendEmail() {
        String subject = languageConf.get("EmailService.reminderHeader");
        String body = languageConf.get("EmailService.reminderBody");
        boolean status = true;
        double convertedAmount = 0;
        try {
            convertedAmount = converter.convert("EUR", transaction.getCurrency(),
                    transaction.getAmount().doubleValue(), transaction.getDate().toInstant());
        } catch (CurrencyConverter.CurrencyConversionException e) {
            status = false;
        } catch (ConnectException e) {
            mainCtrl.handleServerNotFound();
        }

        if(!status){
            System.out.println("Email couldn't be sent");
            Label label = new Label(languageConf.get("ExpandedOpenDebtsListItem.reminderFailed"));
            label.getStyleClass().add("textFont");
            if(detailContainer.getChildren().size() > size)
                detailContainer.getChildren().removeLast();
            detailContainer.getChildren().add(size, label);
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(Currency.getInstance(transaction.getCurrency())
                .getDefaultFractionDigits());
        formatter.setCurrency(Currency.getInstance(transaction.getCurrency()));
        String formattedAmount = formatter.format(convertedAmount);
        body = String.format(body, formattedAmount, transaction.getReceiver().getName(),
                event.getTitle(), event.getId());

        status = emailService.sendEmail(transaction.getGiver().getEmailAddress(),
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