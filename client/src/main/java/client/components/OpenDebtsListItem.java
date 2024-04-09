package client.components;

import client.scenes.MainCtrl;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import commons.Event;
import commons.Participant;
import commons.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

import static com.google.inject.Guice.createInjector;

public class OpenDebtsListItem extends HBox {
    private final LanguageConf languageConf;
    @FXML
    private Label participantLabel;
    ServerUtils server;
    Participant lender;
    Participant debtor;
    double amount;
    Event event;

    public OpenDebtsListItem(Participant lender,
                             Participant debtor,
                             double amount,
                             Event event,
                             LanguageConf languageConf,
                             ServerUtils server) {
        this.lender = lender;
        this.debtor = debtor;
        this.amount = amount;
        this.event = event;
        this.languageConf = languageConf;
        this.server = server;
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/client/components/OpenDebtsListItem.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setResources(languageConf.getLanguageResources());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        String template = languageConf.get("OpenDebtsListItem.template");
        String text = String.format(template, debtor.getName(), lender.getName(), amount);
        participantLabel.setText(text);
    }

    public void onEventClicked(){

    }

    public void settleDebt(){
        Transaction transaction = new Transaction(debtor, lender, amount);
        int status = server.addTransaction(event.getId(), transaction);
        if(status / 100 != 2) {
            System.out.println("server error: " + status);
        }
    }
}
