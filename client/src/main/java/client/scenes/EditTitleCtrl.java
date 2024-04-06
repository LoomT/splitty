package client.scenes;


import client.MockClass.EditEventTitleInterface;
import client.utils.LanguageConf;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditTitleCtrl implements EditEventTitleInterface {

    @FXML
    private TextField nameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Text eventTitle;

    @FXML
    private Text titleError;

    private EventPageCtrl eventPageCtrl;
    private final LanguageConf languageConf;

    /**
     * start screen controller constructor
     *
     * @param languageConf language config instance
     */
    @Inject
    public EditTitleCtrl(
            LanguageConf languageConf
    ) {

        this.languageConf = languageConf;
    }

    /**
     * Initializes the characterLimitError event listener.
     */
    public void initialize(){
        eventTitleListener(nameTextField, titleError, languageConf);
    }

    /**
     * Adds a listener to the titleField which will make the errorField visible
     * with a message informing the user that the
     * length of the text reached maxLength which is currently 50
     *
     * @param titleField event title text field
     * @param errorField error text node
     * @param languageConf languageConf
     */
    static void eventTitleListener(TextField titleField, Text errorField, LanguageConf languageConf) {
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorField.setVisible(false);
            int maxLength = 50;
            if(newValue.length() > maxLength) {
                newValue = newValue.substring(0, maxLength);
            }
            if(newValue.length() == maxLength) {
                errorField.setText(
                        String.format(languageConf.get("StartScreen.maxEventNameLength"), maxLength));
                errorField.setVisible(true);
            }
            titleField.setText(newValue);
        });
    }

    /**
     * Change the title of the EditTitle page
     * @param title new title
     */
    public void changeTitle(String title) {
        eventTitle.setText(title);
    }

    /**
     * Closes the popup.
     */
    @FXML
    public void cancelTitle(){
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Saves the new title if it isn't empty or over 100 characters.
     */
    @FXML
    public void saveTitle(){
        if(eventPageCtrl == null
                || nameTextField.getText().isEmpty()
                || nameTextField.getLength() > 100) return;

        int result = eventPageCtrl.changeTitle(nameTextField.getText());
        if(result >= 400)
            System.out.println("An error has occurred");
        else{
            System.out.println("Event Title changed to: " + nameTextField.getText());
            cancelTitle();
        }
    }

    /**
     * Sets up the EditTitle screen and displays it.
     * @param eventPageCtrl EventPageCtrl which will be changed
     * @param event Event which is displayed
     * @param stage Stage at which the editEventTitle will be displayed
     */
    public void displayEditEventTitle(EventPageCtrl eventPageCtrl, Event event, Stage stage){
        this.eventPageCtrl = eventPageCtrl;
        eventTitle.setText(event.getTitle());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(languageConf.get("TitleChanger.pageTitle"));
        nameTextField.textProperty().setValue("");
        stage.show();
    }
}
