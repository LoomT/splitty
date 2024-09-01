package client.components;

import commons.Tag;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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
    @FXML
    private Label tagLabel;
    private final Runnable onEdit;
    private final Runnable onDelete;

    /**
     * @param description the description of the expense
     * @param participants the participants included in the expense
     * @param tag tag of this expense
     * @param onEdit on edit callback
     * @param onDelete on delete callback
     */
    public ExpenseItem(
            String description,
            String participants,
            Tag tag,
            Runnable onEdit,
            Runnable onDelete
    ) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass()
                        .getResource("/components/ExpenseItem.fxml")
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
        if(tag == null) {
            tagLabel.setVisible(false);
            return;
        }
        String hex = tag.getColor().replace("#", "");
        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);
        Color color = Color.rgb(red, green, blue);
        String textColor = 0.21 * color.getRed() + 0.72 * color.getGreen() + 0.07 * color.getBlue()
                > 0.5 ? "#000000" : "#FFFFFF";
        tagLabel.setStyle("-fx-background-color: #" + color.toString().replace("0x", "")
                + "; -fx-padding: 5px; -fx-text-fill: " + textColor + ";"
                + "-fx-background-radius: 10px;");
        tagLabel.setText(tag.getName());
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
