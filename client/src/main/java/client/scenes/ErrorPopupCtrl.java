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
 * Variables:
 *      mainCtrl: Main controller
 *      LanguageConf: Language configuration
 *      errorHeader: Header of the error
 *      errorDescription: Description of the error
 *      errorImage: Image of the error
 *      errorButton: A button within the error. Currently unused
 * Methods:
 *      generatePopup(String type, String token)
 *          type: Type of error. Used to locate the header and description in the language.conf
 *          This must be compatible with the language.conf format of: ErrorPopup.<type>
 *          token: Field of the error caused. Example: For creating an event token is an Event.
 *          This needs a better name
 *      generatePopup(String type, String token, int limit)
 *          This must be compatible with the language.conf format of: ErrorPopup.<type>
 *          token: Field of the error caused. Example: For creating an event token is an Event.
 *          This needs a better name
 *          limit: word limit for the field.
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
     * @param mainCtrl Main controller
     * @param languageConf language.conf file
     */
    @Inject
    public ErrorPopupCtrl(MainCtrl mainCtrl, LanguageConf languageConf) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
    }

    /**
     *
     * @param token String token to be used
     * @param limit word limit of the input
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
     *
     * @param code code of the error
     * @param prop appropriate language config
     * @param token String token to be used
     * @param limit word limit of the input
     */
    private void errorPicker(ErrorCode code, Properties prop, String token, int limit){
        String header;
        String description;
        switch(code){
            case EmptyStringError -> {
                header = String.format(prop.getProperty("ErrorPopup.emptyFieldErrorHeader"), token);
                description = String.format(
                        prop.getProperty("ErrorPopup.emptyFieldErrorDescription"), token);
            }
            case WordLimitError -> {
                header = String.format(prop.getProperty("ErrorPopup.wordLimitErrorHeader"), token);
                //System.out.println(prop.getProperty("ErrorPopup.wordLimitErrorDescription"));
                description = String.format(
                        prop.getProperty("ErrorPopup.wordLimitErrorDescription"), token, limit);
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

    public enum ErrorCode{
        WordLimitError,
        EmptyStringError,
        InvalidErrorCode

    }


}