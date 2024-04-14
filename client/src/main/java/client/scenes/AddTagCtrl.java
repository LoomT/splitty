package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Tag;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.ConnectException;
import java.util.List;

import static client.utils.CommonFunctions.lengthListener;

public class AddTagCtrl {

    private final MainCtrlInterface mainCtrl;
    private final ServerUtils server;
    private final LanguageConf languageConf;

    @FXML
    private ColorPicker cp;

    @FXML
    private TextField tagTextField;
    @FXML
    private Label warningText;
    @FXML
    private Label confirmationLabel;
    private FadeTransition ft;

    private Color selectedColor;
    private Event event;
    private Stage stage;

    /**
     * @param mainCtrl main controller
     * @param server server utils
     * @param languageConf language config
     */
    @Inject
    public AddTagCtrl(MainCtrlInterface mainCtrl, ServerUtils server,
                                    LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.languageConf = languageConf;

    }

    /**
     * initiliaze method
     */
    public void initialize() {
        cp.setOnAction(event -> {
            if (cp.getValue().equals(Color.WHITE)) {
                showAlert(languageConf.get("AddTag.invalidColour"),
                        languageConf.get("AddTag.whiteNotAllowed"));
                cp.setValue(selectedColor != null ? selectedColor : Color.BLACK);
            } else {
                selectedColor = cp.getValue();
            }
        });

        ft = new FadeTransition(Duration.millis(2000), confirmationLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(1000));
        ft.setOnFinished(e -> confirmationLabel.setVisible(false));
        warningText.setVisible(false);
        lengthListener(tagTextField, warningText, 15, languageConf.get("EditP.nameLimit"));
    }

    /**
     * display the add tag page
     * @param event the current event
     * @param stage the stage of this scene
     */
    public void displayAddTagPage(Event event, Stage stage) {
        this.event = event;
        this.stage = stage;
        confirmationLabel.setVisible(false);
        warningText.setVisible(false);
    }

    /**
     * Back button clicked
     * Closes the stage
     */
    @FXML
    public void backClicked() {
        stage.close();
    }

    /**
     * Add button clicked
     */
    @FXML
    public void addClicked() {
        if (selectedColor == null) {
            showAlert(languageConf.get("AddTag.colnotsel"),
                    languageConf.get("AddTag.colnotselmess"));
        } else if (tagTextField.getText().isEmpty()) {
            showAlert(languageConf.get("AddTag.emptyname"),
                    languageConf.get("AddTag.emptynamemess"));
        } else {
            addButton(event);
        }
    }

    /**
     * Sets colour when colour gets changed
     */
    @FXML
    public void colourChanged() {
        selectedColor = cp.getValue();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * behaviour for add tag button on the page
     *
     * @param event the current event
     */
    public void addButton(Event event) {
        String name = tagTextField.getText();
        if (!name.isEmpty()) {
            String clr = toHexString(selectedColor);
            Tag tag = new Tag(name, clr);
            List<String> tagNames = event.getTags().stream()
                    .map(Tag::getName)
                    .toList();
            if (!tagNames.contains(tag.getName())) {
                try {
                    server.addTag(event.getId(), tag);
                } catch (ConnectException e) {
                    mainCtrl.handleServerNotFound();
                    return;
                }
                tagTextField.clear();
                ft.stop();
                confirmationLabel.setVisible(true);
                confirmationLabel.setOpacity(1.0);
                ft.play();
            }
            else {
                showAlert(languageConf.get("AddTag.alrexist"),
                        languageConf.get("AddTag.alrexistmess"));
            }
        }
    }

    /**
     * convert from color to string
     * @param color chosen colour
     * @return the String color
     */
    private String toHexString(Color color) {
        return String.format("%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    /**
     * Initializes the shortcuts for EditTitle
     *      Enter: saveTitle if the focus is on the textField.
     * @param scene scene the listeners are initialised in
     */
    public void initializeShortcuts(Scene scene){
        CommonFunctions.checkKey(scene, () -> addButton(event), tagTextField, KeyCode.ENTER);
        CommonFunctions.checkKey(scene, () -> this.cp.show(), cp, KeyCode.ENTER);
        CommonFunctions.checkKey(scene, this::backClicked, KeyCode.ESCAPE);
    }
}
