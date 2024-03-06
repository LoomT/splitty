package client.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.function.Consumer;

public class EventListItem extends HBox {
    @FXML
    private Button button;
    @FXML
    private Label label;

    private String eventName;
    private Runnable onRemoveCallback;
    private Consumer<String> onClickCallback;


    /**
     * @param eventName the name to display
     * @param onRemove the callback to be called when the remove button ic clicked
     */
    public EventListItem(String eventName, Runnable onRemove, Consumer<String> onClick) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass()
                        .getResource("/client/components/EventListItem.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.eventName = eventName;
        this.onRemoveCallback = onRemove;
        this.onClickCallback = onClick;
        label.setText(eventName);
    }


    /**
     * This is run when the x button is clicked
     */
    @FXML
    private void onXClick() {
        onRemoveCallback.run();
    }

    @FXML
    private void eventCodeClicked() {
        onClickCallback.accept(this.eventName);
    }
}
