package client.components;

import client.scenes.MainCtrl;
import client.utils.LanguageConf;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

import java.util.MissingResourceException;

/**
 * Controller for the error popup.
 * A default error popup is initialized first and then the fields are changed
 * according to the situation.
 * Fields:
 * mainCtrl: Main controller
 * LanguageConf: Language configuration
 * errorHeader: Header of the error
 * errorDescription: Description of the error
 * errorImage: Image of the error
 * errorButton: A button within the error. Currently unused as a placeholder
 * Methods:
 * generatePopup (ErrorCode code, String stringToken, int intToken)
 * Variables:
 * ErrorCode code: code of the error. Used to specify the error.
 * Can be extended using ErrorCode enum
 * stringToken, intToken: tokens that can be used within errors as variables.
 * The generatePopup changes the header and the description of the
 * ErrorPopup.fxml found in scenes
 * according to the properties found in the languageConfig and
 * the code of the error.
 */
public class ErrorPopupCtrl {

    private final MainCtrl mainCtrl;
    private final LanguageConf languageConf;
    @FXML
    private Text errorHeader;
    @FXML
    private Button errorButton;
    @FXML
    private Text errorDescription;
    @FXML
    private ImageView errorImage;

    /**
     * initializes fields
     */
    @FXML
    public void initialize() {

    }

    /**
     * Constructor for the ErrorPopupCtrl
     *
     * @param mainCtrl     Main controller of the client
     * @param languageConf language.conf file of the client
     */
    @Inject
    public ErrorPopupCtrl(MainCtrl mainCtrl, LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
    }

    /**
     * Generates a popup using the code and the tokens.
     *
     * @param stringToken String stringToken to be used
     * @param intToken    word intToken of the input
     * @param code        code of the error as found in the properties file
     */
    public void generatePopup(String code, String stringToken, int intToken) {
        String header;
        String description;
        try {
            this.errorImage.setImage(new Image(String.valueOf(
                    getClass().getResource("/client/scenes/icons8-error-96.png"))));
            stringToken = languageConf.get(stringToken);

            header = String.format(languageConf.get(
                    "ErrorPopup." + code + "Header"), stringToken, intToken);
            description = String.format(languageConf.get(
                    "ErrorPopup." + code + "Description"), stringToken, intToken);
        } catch (MissingResourceException e) {
            header = languageConf.get("ErrorPopup.unknownErrorHeader");
            description = languageConf.get("ErrorPopup.unknownErrorDescription");
        }
        this.errorHeader.setText(header);
        this.errorDescription.setText(description);
    }

    /**
     * ErrorCode enum should be used to classify error codes.
     */
    public enum ErrorCode {
        WordLimitError,
        EmptyStringError,
        InvalidErrorCode

    }


}