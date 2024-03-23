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
     * @param type
     * @param token
     */
    public void generatePopup(String type, String token){
        String languageURL = Objects.requireNonNull(getClass().getResource
                ("/languages_" + languageConf.getCurrentLocaleString() + ".properties")).getPath();
        try(FileInputStream fis = new FileInputStream(languageURL)){
            this.errorImage.setImage(new Image(String.valueOf(
                    getClass().getResource("/client/scenes/icons8-error-96.png"))));
            Properties prop = new Properties();
            prop.load(fis);

            this.errorHeader.setText(String.format(
                    prop.getProperty("ErrorPopup." + type + "Header"), token));
            this.errorDescription.setText(String.format(
                    prop.getProperty("ErrorPopup." + type + "Description"), token));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param type
     * @param token
     * @param limit
     */
    public void generatePopup(String type, String token, int limit){ //get a better name
        String languageURL = Objects.requireNonNull(getClass().getResource
                ("/languages_" + languageConf.getCurrentLocaleString() + ".properties")).getPath();
        try(FileInputStream fis = new FileInputStream(languageURL)){
            this.errorImage.setImage(new Image(String.valueOf(
                    getClass().getResource("/client/scenes/icons8-error-96.png"))));
            Properties prop = new Properties();
            prop.load(fis);

            this.errorHeader.setText(prop.getProperty("ErrorPopup." + type + "Header"));
            this.errorDescription.setText(String.format(
                    prop.getProperty("ErrorPopup." + type + "Description"), token, limit));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}