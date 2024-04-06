package client.components;

import commons.Expense;
import commons.Participant;
import jakarta.servlet.http.Part;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class OpenDebtsListItem {
    @FXML
    private ChoiceBox<Pane> expandableView;
    @FXML
    private Label participantLabel;

    public OpenDebtsListItem(Participant debtor, Participant lender, double amount) {
        String text = debtor.getName() + " owes " + lender.getName() + " " + amount;
        participantLabel.setText(text);
    }
}
