package client.scenes;

import client.utils.LanguageConf;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Controller for the error popup.
 * A default error popup is initialized first and then the fields are changed
 * according to the situation.
 * Fields:
 *      mainCtrl: Main controller
 *      LanguageConf: Language configuration
 *      errorHeader: Header of the error
 *      errorDescription: Description of the error
 *      errorImage: Image of the error
 *      errorButton: A button within the error. Currently unused as a placeholder
 * Methods:
 *      generatePopup (ErrorCode code, String stringToken, int intToken)
 *      Variables:
 *          ErrorCode code: code of the error. Used to specify the error.
 *          Can be extended using ErrorCode enum
 *          stringToken, intToken: tokens that can be used within errors as variables.
 *      The generatePopup changes the header and the description of the
 *      ErrorPopup.fxml found in scenes
 *      according to the properties found in the languageConfig and
 *      the code of the error.
 * <p>
 *      errorPicker(ErrorCode code, Properties prop, String stringToken, int intToken)
 *      Variables:
 *      ErrorCode code, stringToken, intToken: same as the generatePopup
 *      Properties prop: properties file that has the current language config of the client
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
    public void initialize(){

    }

    /**
     * Constructor for the ErrorPopupCtrl
     * @param mainCtrl Main controller of the client
     * @param languageConf language.conf file of the client
     */
    @Inject
    public ErrorPopupCtrl(MainCtrl mainCtrl, LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
    }

    /**
     * Generates a popup using the code and the tokens.
     * @param token String token to be used
     * @param limit word limit of the input
     * @param code code of the error as found in the ErrorCode enum
     */
    public void generatePopup(ErrorCode code, String token, int limit){

        String languageURL = Objects.requireNonNull(getClass().getResource
                ("/languages_" + languageConf.getCurrentLocaleString() + ".properties")).getPath();
        try(FileInputStream fis = new FileInputStream(languageURL)){
            this.errorImage.setImage(new Image(String.valueOf(
                    getClass().getResource("/client/scenes/icons8-error-96.png"))));
            Properties prop = new Properties();
            prop.load(fis);
            errorPicker(code, prop, token, limit);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Picks the correct header and description for the error depending on the code given.
     * If the code cannot be found, gives an unknown error header and description.
     * @param code code of the error as found in the ErrorCodes Enum
     * @param prop language config file the client is currently at
     * @param stringToken String token to be used in the error text as a variable
     * @param intToken an integer token to be used as a variable in the error text
     */
    private void errorPicker(ErrorCode code, Properties prop, String stringToken, int intToken){
        String header;
        String description;
        switch(code){
            case EmptyStringError -> {
                header = String.format(prop.getProperty(
                        "ErrorPopup.emptyFieldErrorHeader"), stringToken);
                description = String.format(prop.getProperty(
                        "ErrorPopup.emptyFieldErrorDescription"), stringToken);
            }
            case WordLimitError -> {
                header = String.format(prop.getProperty(
                        "ErrorPopup.wordLimitErrorHeader"), stringToken);
                description = String.format(prop.getProperty(
                        "ErrorPopup.wordLimitErrorDescription"), stringToken, intToken);
            }

            case InvalidErrorCode -> {
                header = prop.getProperty("ErrorPopup.invalidJoinCodeErrorHeader");
                description = prop.getProperty("ErrorPopup.invalidJoinCodeErrorDescription");
            }
            default -> {
                header = prop.getProperty("ErrorPopup.unknownErrorHeader");
                description = prop.getProperty("ErrorPopup.unknownErrorDescription");
            }
        }
        this.errorHeader.setText(header);
        this.errorDescription.setText(description);
    }

    /**
     * ErrorCode enum should be used to classify error codes.
     */
    public enum ErrorCode{
        WordLimitError,
        EmptyStringError,
        InvalidErrorCode

    }


}