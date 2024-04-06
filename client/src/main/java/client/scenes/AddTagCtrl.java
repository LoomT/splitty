package client.scenes;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.List;

public class AddTagCtrl {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final LanguageConf languageConf;

    @FXML
    private Button add;

    @FXML
    private Button back;

    @FXML
    private ColorPicker cp;

    @FXML
    private TextField tagTextField;

    private Color selectedColor;

    /**
     * @param mainCtrl main controller
     * @param server server utils
     * @param languageConf language config
     */
    @Inject
    public AddTagCtrl(MainCtrl mainCtrl, ServerUtils server,
                                    LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.languageConf = languageConf;

    }

    /**
     * initiliaze method
     */
    public void initialize() {

    }

    /**
     * display the add tag page
     * @param ev the current event
     */
    public void displayAddTagPage(Event ev) {
        cp.setOnAction(e -> selectedColor = cp.getValue());
        add.setOnAction(e -> {
            if (selectedColor == null) {
                showAlert(languageConf.get("AddTag.colnotsel"),
                        languageConf.get("AddTag.colnotselmess"));
            } else if (tagTextField.getText().isEmpty()) {
                showAlert(languageConf.get("AddTag.emptyname"),
                        languageConf.get("AddTag.emptynamemess"));
            } else {
                addButton(ev);
            }
        });
        back.setOnAction(e -> {
            mainCtrl.showAddExpensePage(ev);
            System.out.println(ev.getTags());
        });
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
                server.addTag(event.getId(), tag);
                tag.setEventID(event.getId());
            }
            else {
                showAlert(languageConf.get("AddTag.alrexist"),
                        languageConf.get("AddTag.alrexistmess"));
            }
            tagTextField.clear();
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




}
