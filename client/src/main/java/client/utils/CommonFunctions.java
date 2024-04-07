package client.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CommonFunctions {
    /**
     * Adds a listener to the textField which will make the warningLabel visible
     * with a given message informing the user that the
     * length of the text reached maxLength
     *
     * @param textField event title text field
     * @param warningLabel error text node
     * @param maxLength max length of the field
     * @param warningMessage localized message with %d for showing max length
     */
    public static void lengthListener(TextField textField, Label warningLabel,
                                      int maxLength, String warningMessage) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            warningLabel.setVisible(false);
            if(newValue.length() > maxLength) {
                newValue = newValue.substring(0, maxLength);
            }
            if(newValue.length() == maxLength) {
                warningLabel.setText(
                        String.format(warningMessage, maxLength));
                warningLabel.setVisible(true);
            }
            textField.setText(newValue);
        });
    }
}
