package client.components;

import client.utils.LanguageConf;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.util.Optional;

public class Confirmation {
    private final Alert alert;

    /**
     * Constructs a confirmation pop-up with yes and cancel buttons
     *
     * @param message info message
     * @param header header title
     * @param languageConf language config
     */
    public Confirmation(String message, String header, LanguageConf languageConf) {
        alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.YES, ButtonType.CANCEL);
        alert.setHeaderText(header);
        alert.setTitle(languageConf.get("Confirmation.confirmation"));
        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES))
                .setText(languageConf.get("yes"));
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL))
                .setText(languageConf.get("cancel"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }

    /**
     * Shows the confirmation pop-up and awaits user input
     *
     *
     * @return ButtonType.YES or ButtonType.CANCEL
     */
    public Optional<ButtonType> showAndWait() {
        return alert.showAndWait();
    }
}
