package client.components;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.DecimalFormat;

public class ExpandedOpenDebtsListItem extends HBox {
    @FXML
    private Label participantLabel;
    @FXML
    private Text availability;
    @FXML
    private Text accountHolder;
    @FXML
    private Text iban;
    @FXML
    private Text bic;
    private final ServerUtils server;
    private final Participant lender;
    private final Participant debtor;
    private final double amount;
    private final Event event;
    private final MainCtrlInterface mainCtrl;

    /**
     * Constructor for ExpandedOpenDebtsListItem
     *
     * @param lender       lender of the debt
     * @param debtor       debtor of the debt
     * @param amount       amount owed
     * @param event        Event the debt is in
     * @param languageConf languageConf of the page
     * @param server       server the client is connected to
     * @param mainCtrl     main Controller of the client
     */
    public ExpandedOpenDebtsListItem(Participant lender,
                                     Participant debtor,
                                     double amount,
                                     Event event,
                                     LanguageConf languageConf,
                                     ServerUtils server,
                                     MainCtrlInterface mainCtrl) {
        this.lender = lender;
        this.debtor = debtor;
        this.amount = amount;
        this.event = event;
        this.mainCtrl = mainCtrl;
        this.server = server;
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
            throw new RuntimeException(exception);
        }

        DecimalFormat numberFormat = new DecimalFormat("#.00");
        String template = languageConf.get("OpenDebtsListItem.template");
        String text = String.format(template, debtor.getName(),
                lender.getName(), numberFormat.format(amount));
        participantLabel.setText(text);
    }

    /**
     * contracts the item
     */
    public void onEventClicked() {
        mainCtrl.resizeOpenDebtItem(this);
    }

    /**
     * initializes the expanded Bank account information fields
     *
     * @param languageConf languageConf of the page
     */
    public void initializeFields(LanguageConf languageConf) {
        if ((debtor.getBeneficiary() == null
                && debtor.getAccountNumber() == null)
                || (debtor.getAccountNumber().isEmpty()
                && debtor.getBeneficiary().isEmpty())) {
            availability.setText(languageConf.get("ExpandedOpenDebtsListItem.bankAccountEmpty"));
            return;
        }

        if (debtor.getBeneficiary() != null && !debtor.getBeneficiary().isEmpty()) {
            availability.setText(languageConf.get(
                    "ExpandedOpenDebtsListItem.bankAccountPartiallyFull"));
            accountHolder.setText("Beneficiary:" + debtor.getBeneficiary());
            if (debtor.getAccountNumber() != null && !debtor.getAccountNumber().isEmpty()) {
                availability.setText(languageConf.get(
                        "ExpandedOpenDebtsListItem.bankAccountFull"));
                iban.setText("IBAN: " + debtor.getAccountNumber());
            }
        } else {
            availability.setText(languageConf.get(
                    "ExpandedOpenDebtsListItem.bankAccountPartiallyFull"));
            iban.setText("IBAN: " + debtor.getAccountNumber());
        }
    }

    /**
     * Settles the debt displayed in the item
     */
    public void settleDebt() {
        mainCtrl.settleDebt(debtor, lender, amount, event, server);
    }


    /**
     * getter for lender
     *
     * @return lender
     */
    public Participant getLender() {
        return lender;
    }

    /**
     * getter for debtor
     *
     * @return debtor
     */
    public Participant getDebtor() {
        return debtor;
    }

    /**
     * getter for amount
     *
     * @return amount
     */
    public double getAmount() {
        return amount;
    }
}