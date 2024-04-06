package client.components;

import client.MyModule;
import client.utils.LanguageConf;
import com.google.inject.Injector;
import commons.Expense;
import commons.Participant;
import jakarta.servlet.http.Part;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ResourceBundle;

import static com.google.inject.Guice.createInjector;

public class OpenDebtsListItem extends HBox {
    @FXML
    private Label participantLabel;
    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final LanguageConf languageConf = INJECTOR.getInstance(LanguageConf.class);

    public OpenDebtsListItem(Participant debtor, Participant lender, double amount) {
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
        String text = debtor.getName() + " owes " + lender.getName() + " " + amount;
        participantLabel.setText(text);
    }

    public void onEventClicked(){
        System.out.println("test");
    }
}
