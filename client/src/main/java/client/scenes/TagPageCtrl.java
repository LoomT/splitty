package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import client.utils.currency.CurrencyConverter;
import com.google.inject.Inject;
import commons.Event;
import commons.Tag;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.util.Optional;

public class TagPageCtrl {

    @FXML
    private VBox tagList;
    @FXML
    private Button back;
    @FXML
    private ColorPicker colorPicker;

    private final Websocket websocket;
    private final CurrencyConverter converter;
    private final UserConfig userConfig;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final ServerUtils server;

    /**
     * @param mainCtrl     mainCtrl injection
     * @param languageConf the language config instance
     * @param websocket    the websocket instance
     * @param server       server to be ysed
     * @param converter currency converter
     * @param userConfig user config
     */
    @Inject

    public TagPageCtrl(MainCtrlInterface mainCtrl, LanguageConf languageConf,
                         Websocket websocket, ServerUtils server, CurrencyConverter converter,
                         UserConfig userConfig) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.server = server;
        this.websocket = websocket;
        this.converter = converter;
        this.userConfig = userConfig;
    }

    public void initialize() {

    }
    public void displayTagPage(Event event) {
        populateTagList(event);
        back.setOnAction(e -> {
            mainCtrl.showStatisticsPage(event); // pass updated tags
        });
    }

    public void populateTagList(Event event) {
        tagList.getChildren().clear();
        for (Tag tag : event.getTags()) {
            String tagName = tag.getName();
            Label label = new Label(tagName);
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 15));

            Shape coloredBox = new Rectangle(15, 15);
            // Set tag color
            coloredBox.setFill(Color.web(tag.getColor()));

            // Add event handler to open color picker dialog
            coloredBox.setOnMouseClicked(e -> {
                // Create color picker dialog
                Stage colorPickerStage = new Stage();
                colorPickerStage.setTitle("Change colour"); // Set title here
                colorPicker = new ColorPicker(Color.web(tag.getColor()));
                colorPicker.setOnAction(event1 -> {
                    // Update tag's color
                    tag.setColor(colorPicker.getValue().toString());
                    try {
                        server.updateTag(tag.getId(), event.getId(), tag);
                    } catch (ConnectException ex) {
                        mainCtrl.handleServerNotFound();
                        return;
                    }

                    // Update UI
                    populateTagList(event);

                    // Close color picker dialog
                    colorPickerStage.close();
                });

                // Set the width of the color picker dialog scene
                Scene colorPickerScene = new Scene(colorPicker, 200, 50); // Adjust width as needed
                colorPickerStage.setScene(colorPickerScene);
                colorPickerStage.show();
            });

            // Create delete button
            Button deleteButton = new Button("X");
            deleteButton.setOnAction(e -> {
                String tagNameToRemove = tag.getName();
                ObservableList<Node> temp = tagList.getChildren();
                for (Node node : temp) {
                    if (node instanceof HBox hBox) {
                        for (Node child : hBox.getChildren()) {
                            if (child instanceof Label lab) {
                                if (tagNameToRemove.equals(lab.getText())) {
                                    // Remove the HBox (which contains both the tag and delete button) from the VBox
                                    tagList.getChildren().remove(hBox);
                                    try {
                                        //event.getTags().remove(tag);
                                        server.deleteTag(tag.getId(), event.getId());
                                    } catch (ConnectException ex) {
                                        mainCtrl.handleServerNotFound();
                                        return;
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
                // You may want to handle tag deletion from the backend here
            });

            // Create edit button
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> {
                // Create text input dialog for renaming the tag
                TextInputDialog dialog = new TextInputDialog(tag.getName());
                dialog.setTitle("Edit Tag Name");
                dialog.setHeaderText(null);
                dialog.setContentText("Enter new tag name:");

                // Get the result of the dialog
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newTagName -> {
                    // Update the tag's name
                    tag.setName(newTagName);
                    try {
                        server.updateTag(tag.getId(), event.getId(), tag);
                    } catch (ConnectException ex) {
                        mainCtrl.handleServerNotFound();
                        return;
                    }

                    // Update UI
                    populateTagList(event);
                });
            });

            // Set minimum width for label to ensure full visibility
            label.setMinWidth(Label.USE_PREF_SIZE);

// Set minimum width for edit button to ensure full visibility
            editButton.setMinWidth(Button.USE_PREF_SIZE);

// Set minimum width for delete button to ensure full visibility
            deleteButton.setMinWidth(Button.USE_PREF_SIZE);

            // Create an HBox to contain the color box, label, edit button, and delete button
            HBox tagItem = new HBox(15);
            tagItem.getChildren().addAll(coloredBox, label, editButton, deleteButton);

            // Add the HBox to the tagList VBox
            tagList.getChildren().add(tagItem);
        }
    }


}
