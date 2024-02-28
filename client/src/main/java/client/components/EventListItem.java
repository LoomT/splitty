package client.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class EventListItem extends HBox {
    @FXML
    private Button button;
    @FXML
    private Label label;

    private String eventName;
    Runnable onRemoveCallback;


    public EventListItem(String eventName, Runnable onRemove) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/EventListItem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.eventName = eventName;
        this.onRemoveCallback = onRemove;
        label.setText(eventName);
    }


    @FXML
    private void onXClick() {
        System.out.println("X on button " + this.eventName);
        onRemoveCallback.run();
    }
}
