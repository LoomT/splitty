package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.components.Confirmation;
import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Tag;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.util.Optional;

import static commons.WebsocketActions.*;
import static java.lang.String.format;


public class TagPageCtrl {

    @FXML
    private VBox tagList;
    @FXML
    private Button back;
    @FXML
    private Button addTag;


    private final Websocket websocket;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final ServerUtils server;
    private Event event;

    /**
     * @param mainCtrl     mainCtrl injection
     * @param languageConf the language config instance
     * @param websocket    the websocket instance
     * @param server       server to be ysed
     */
    @Inject

    public TagPageCtrl(MainCtrlInterface mainCtrl, LanguageConf languageConf,
                         Websocket websocket, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.server = server;
        this.websocket = websocket;
    }

    /**
     * initialize method
     */
    public void initialize() {

        back.setOnAction(e -> mainCtrl.showStatisticsPage(event));
        websocket.on(REMOVE_TAG, t -> populateTagList(event));
        websocket.on(UPDATE_TAG, t -> populateTagList(event));
        websocket.on(ADD_TAG, t -> populateTagList(event));
    }

    /**
     * method for displaying the page for editing tags
     * @param event the current event
     */
    public void displayTagPage(Event event) {
        this.event = event;
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

        Region region = new Region();
        region.setPrefSize(0, 0);
        HBox tagItem = new HBox(10, coloredBox, label, region, editButton, deleteButton);
        HBox.setHgrow(region, Priority.ALWAYS);
        tagItem.setAlignment(Pos.CENTER_LEFT);
        tagItem.getStyleClass().add("eventListItemContainer");
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
        coloredBox.setCursor(Cursor.HAND);
        coloredBox.setFocusTraversable(true);
        return coloredBox;
    }

    private Button createEditButton(Tag tag, Event event) {
        Button editButton = new Button("\uD83D\uDD89");
        editButton.setOnAction(e -> showEditDialog(tag, event));
        editButton.setMinWidth(Button.USE_PREF_SIZE);
        editButton.getStyleClass().add("sbutton");
        return editButton;
    }

    private Button createDeleteButton(Tag tag, Event event) {
        Button deleteButton = new Button("\u274C");
        deleteButton.setOnAction(e -> deleteTag(tag, event));
        deleteButton.setMinWidth(Button.USE_PREF_SIZE);
        deleteButton.getStyleClass().add("sbutton");
        return deleteButton;
    }

    private void showColorPicker(Tag tag, String color) {
        Stage colorPickerStage = new Stage();
        colorPickerStage.initModality(Modality.APPLICATION_MODAL);
        colorPickerStage.setTitle(languageConf.get("AddTag.choosecolor"));
        ColorPicker colorPicker = new ColorPicker(Color.web(color));
        colorPicker.getStyleClass().add("sbutton");
        colorPicker.setOnAction(e -> {
            tag.setColor(colorPicker.getValue().toString().replace("0x", "#"));
            try {
                server.updateTag(tag.getId(), event.getId(), tag);
            } catch (ConnectException ex) {
                mainCtrl.handleServerNotFound();
                return;
            }
            colorPickerStage.close();
        });
        Scene colorPickerScene = new Scene(colorPicker, 200, 50);
        colorPickerStage.setScene(colorPickerScene);
        colorPickerStage.show();
    }

    private void showEditDialog(Tag tag, Event event) {
        TextInputDialog dialog = new TextInputDialog(tag.getName());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setGraphic(null);
        dialogPane.getStylesheets().add(getClass().getResource("global.css").toExternalForm());
        dialogPane.getStyleClass().add("backgroundAnchorpane");
        dialog.getEditor().getStyleClass().add("ntextfield");
        dialog.setTitle("Edit Tag Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new tag name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newTagName -> {
            if(event.getTags().stream().anyMatch(t -> t.getName().equals(newTagName))) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        languageConf.get("AddTag.alrexistmess"));
                alert.setTitle(languageConf.get("AddTag.alrexist"));
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            tag.setName(newTagName);
            try {
                server.updateTag(tag.getId(), event.getId(), tag);
            } catch (ConnectException ex) {
                mainCtrl.handleServerNotFound();
                return;
            }
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
                            Confirmation confirmation =
                                    new Confirmation((format(
                                            languageConf.get(
                                                    "TagPage.deleteTagConfirmation"),
                                            "")),
                                            languageConf.get("Confirmation.areYouSure"),
                                            languageConf);
                            Optional<ButtonType> result = confirmation.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.YES) {
                                try {
                                    server.deleteTag(tag.getId(), event.getId());
                                } catch (ConnectException ex) {
                                    mainCtrl.handleServerNotFound();
                                }
                            }
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
    }

    /**
     * Initializes the shortcuts for Tag page:
     *      Escape: go back
     * @param scene scene the listeners are initialised in
     */
    public void initializeShortcuts(Scene scene) {
        CommonFunctions.checkKey(scene, () -> mainCtrl.showStatisticsPage(event), KeyCode.ESCAPE);
    }
}
