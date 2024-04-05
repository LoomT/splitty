package client.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;


public class DebtListItem extends HBox {

    @FXML
    private Label debtTitleLabel;

    /**
     * @param debtName the name to display
     */
    public DebtListItem(String debtName) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass()
                        .getResource("/client/components/DebtListItem.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.debtTitleLabel.setText(debtName);
    }
}
