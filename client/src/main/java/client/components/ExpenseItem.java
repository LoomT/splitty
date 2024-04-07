package client.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;


public class ExpenseItem extends HBox {
    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label participantsLabel;
    private Runnable onEdit;
    private Runnable onDelete;

    public ExpenseItem(String description, String participants, Runnable onEdit, Runnable onDelete) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass()
                        .getResource("/client/components/ExpenseItem.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        descriptionLabel.setText(description);
        participantsLabel.setText(participants);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        editButton.setText("\uD83D\uDD89");
        deleteButton.setText("\u274C");
    }

    @FXML
    private void onEditClicked() {
        onEdit.run();
    }

    @FXML
    private void onDeleteClicked() {
        onDelete.run();
    }
}
