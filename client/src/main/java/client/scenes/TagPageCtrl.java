package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.Websocket;
import client.utils.currency.CurrencyConverter;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static commons.WebsocketActions.*;


public class TagPageCtrl {

    @FXML
    private VBox tagList;
    @FXML
    private Button back;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button addTag;

    private List<Tag> removedTags = new ArrayList<>();

    private final Websocket websocket;
    private final CurrencyConverter converter;
    private final UserConfig userConfig;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final ServerUtils server;
    private Event event;
    private List<Tag> currentTags;

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
        currentTags = new ArrayList<>();
    }

    /**
     * initialize method
     */
    public void initialize() {

        back.setOnAction(e -> {
            mainCtrl.showStatisticsPage(event); // pass updated tags
        });
        websocket.on(REMOVE_TAG, t -> {
            Tag tag = (Tag) t;
            event.getTags().remove(tag);
            for (Expense exp : event.getExpenses()) {
                if (exp.getType().getId() == tag.getId()) {
                    exp.setType(null);
                }
            }
            populateTagList(event);
        });
        websocket.on(UPDATE_TAG, t -> {
            Tag tag = (Tag) t;
            for (int i = 0; i < event.getTags().size(); i++) {
                if (event.getTags().get(i).getId() == tag.getId()) {
                    event.getTags().set(i, tag);
                }
            }
            for (Expense exp : event.getExpenses()) {
                if (exp.getType().getId() == tag.getId()) {
                    exp.setType(tag);
                }
            }
            populateTagList(event);
        });
        websocket.on(ADD_TAG, t -> {
            populateTagList(event);
        });
    }

    /**
     * method for displaying the page for editting tags
     * @param event the current event
     */
    public void displayTagPage(Event event) {
        this.event = event;
        currentTags.clear();
        for (Tag t : event.getTags()) {
            currentTags.add(t);
        }
        populateTagList(event);
    }


    /**
     * method for populating the tag list
     * @param event the current event
     */
    public void populateTagList(Event event) {
        tagList.getChildren().clear();

        for (Tag tag : event.getTags()) {
            HBox tagItem = createTagItem(tag, event);
            tagList.getChildren().add(tagItem);
        }
    }

    private HBox createTagItem(Tag tag, Event event) {
        String tagName = tag.getName();
        Label label = createTagLabel(tagName);
        Shape coloredBox = createColoredBox(tag, tag.getColor());
        Button editButton = createEditButton(tag, event);
        Button deleteButton = createDeleteButton(tag, event);

        HBox tagItem = new HBox(15);
        tagItem.getChildren().addAll(coloredBox, label, editButton, deleteButton);

        return tagItem;
    }

    private Label createTagLabel(String tagName) {
        Label label = new Label(tagName);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        label.setMinWidth(Label.USE_PREF_SIZE); // Ensure full visibility of the words
        return label;
    }

    private Shape createColoredBox(Tag tag, String color) {
        Shape coloredBox = new Rectangle(15, 15);
        coloredBox.setFill(Color.web(color));
        coloredBox.setOnMouseClicked(e -> showColorPicker(tag, color));
        return coloredBox;
    }

    private Button createEditButton(Tag tag, Event event) {
        Button editButton = new Button("Edit name");
        editButton.setOnAction(e -> showEditDialog(tag, event));
        editButton.setMinWidth(Button.USE_PREF_SIZE);
        return editButton;
    }

    private Button createDeleteButton(Tag tag, Event event) {
        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> deleteTag(tag, event));
        deleteButton.setMinWidth(Button.USE_PREF_SIZE);
        return deleteButton;
    }

    private void showColorPicker(Tag tag, String color) {
        Stage colorPickerStage = new Stage();
        colorPickerStage.setTitle("Change colour");
        colorPicker = new ColorPicker(Color.web(color));
        colorPicker.setOnAction(e -> {
            tag.setColor(colorPicker.getValue().toString());
            try {
                server.updateTag(tag.getId(), event.getId(), tag);
            } catch (ConnectException ex) {
                mainCtrl.handleServerNotFound();
                return;
            }
            populateTagList(event);
            colorPickerStage.close();
        });
        Scene colorPickerScene = new Scene(colorPicker, 200, 50);
        colorPickerStage.setScene(colorPickerScene);
        colorPickerStage.show();
    }

    private void showEditDialog(Tag tag, Event event) {
        TextInputDialog dialog = new TextInputDialog(tag.getName());
        dialog.setTitle("Edit Tag Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new tag name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newTagName -> {
            tag.setName(newTagName);
            try {
                server.updateTag(tag.getId(), event.getId(), tag);
            } catch (ConnectException ex) {
                mainCtrl.handleServerNotFound();
                return;
            }
            populateTagList(event);
        });
    }

    private void deleteTag(Tag tag, Event event) {
        String tagNameToRemove = tag.getName();
        ObservableList<Node> temp = tagList.getChildren();
        for (Node node : temp) {
            if (node instanceof HBox hBox) {
                for (Node child : hBox.getChildren()) {
                    if (child instanceof Label lab) {
                        if (tagNameToRemove.equals(lab.getText())) {
                            tagList.getChildren().remove(hBox);
                            try {
                                event.getTags().remove(tag);
                                server.deleteTag(tag.getId(), event.getId());
                                for (Expense exp : event.getExpenses()) {
                                    if (exp.getType().getId() == tag.getId()) {
                                        exp.setType(null);
                                    }
                                }
                                removedTags.add(tag);
                            } catch (ConnectException ex) {
                                mainCtrl.handleServerNotFound();
                                return;
                            }
                            populateTagList(event);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * behaviour for the button add tag
     */
    public void addTagClicked() {
        mainCtrl.showAddTagPage(event);
        populateTagList(event);
    }
}
