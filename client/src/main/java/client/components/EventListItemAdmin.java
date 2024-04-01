package client.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class EventListItemAdmin extends HBox {
    @FXML
    private Button button;
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label eventCodeLabel;
    private Runnable onRemoveCallback;
    private Runnable onDownloadClickCallback;
    private Runnable onClickCallback;


    /**
     * @param eventName the event title to display
     * @param id the event id to display
     * @param onRemove the callback to be called when the remove button is clicked
     * @param onDownload the callback to be called when download button is clicked
     * @param onClick the callback for when the event is clicked
     */
    public EventListItemAdmin(String eventName, String id, Runnable onRemove,
                              Runnable onDownload, Runnable onClick) {
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass()
                .getResource("/client/components/EventListItemAdmin.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.onRemoveCallback = onRemove;
        this.onDownloadClickCallback = onDownload;
        this.onClickCallback = onClick;
        eventTitleLabel.setText(eventName);
        eventCodeLabel.setText(id);
    }


    /**
     * This is run when the x button is clicked
     */
    @FXML
    private void onXClick() {
        onRemoveCallback.run();
    }

    /**
     * This is run when download button is clicked
     */
    @FXML
    private void onDownloadClick() {
        onDownloadClickCallback.run();
    }
    @FXML
    private void onEventClicked() {
        onClickCallback.run();
    }

    /**
     * @return event code of the list item
     */
    public Label getEventCodeLabel() {
        return eventCodeLabel;
    }
}
