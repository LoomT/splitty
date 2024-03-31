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
    private Label eventCodeLabel;

    @FXML
    private Label eventTitleLabel;

    private String eventName;

    private String id;
    private Runnable onRemoveCallback;
    private Consumer<String> onClickCallback;


    /**
     * @param eventName the name to display
     * @param id the event id to display
     * @param onRemove the callback to be called when the remove button ic clicked
     * @param onClick the callback for when the eventcode is clicked
     */
    public EventListItem(String eventName, String id, Runnable onRemove, Consumer<String> onClick) {
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
        this.id = id;
        this.onRemoveCallback = onRemove;
        this.onClickCallback = onClick;
        eventTitleLabel.setText(eventName);
        eventCodeLabel.setText(id);    }


    /**
     * This is run when the x button is clicked
     */
    @FXML
    private void onXClick() {
        onRemoveCallback.run();
    }

    @FXML
    private void eventCodeClicked() {
        onClickCallback.accept(this.id);
    }
}
