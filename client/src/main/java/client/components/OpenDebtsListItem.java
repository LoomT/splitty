package client.components;

import client.utils.LanguageConf;
import commons.Participant;
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

    public OpenDebtsListItem(String template, Participant debtor,
                             Participant lender,
                             double amount,
                             LanguageConf languageConf) {

        this.languageConf = languageConf;
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
        template = languageConf.get(template);
        String text = String.format(template, debtor.getName(), lender.getName(), amount);
        participantLabel.setText(text);
    }

    public void onEventClicked(){
        System.out.println("test");
    }
}
