package client.scenes;

import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static client.utils.CommonFunctions.getHighContrastEffect;

public class OptionsCtrl {

    private final UserConfig userConfig;
    private final LanguageConf languageConf;
    private final CurrencyConverter converter;
    private final ServerUtils server;
    @FXML
    private ComboBox<CommonFunctions.HideableItem<String>> currencyChoiceBox;
    @FXML
    private TextField serverField;
    @FXML
    private ToggleButton contrastToggle;
    @FXML
    private Label confirmationLabel;
    private Stage stage;
    private FadeTransition ft;
    private boolean lastContrast;

    /**
     * @param userConfig user configuration
     * @param languageConf language configuration
     * @param converter currency converter
     * @param server server utils
     */
    @Inject
    public OptionsCtrl(UserConfig userConfig, LanguageConf languageConf,
                       CurrencyConverter converter, ServerUtils server) {
        this.userConfig = userConfig;
        this.languageConf = languageConf;
        this.converter = converter;
        this.server = server;
    }

    /**
     * Initialize all fields and some nodes
     */
    public void initialize() {
        CommonFunctions.comboBoxAutoCompletionSupport(converter.getCurrencies(),
                currencyChoiceBox);
        String cur = userConfig.getCurrency();
        if(!cur.equals("None")) {
            CommonFunctions.HideableItem<String> item =
                    currencyChoiceBox.getItems().stream()
                            .filter(i -> i.toString().equals(cur)).findFirst().orElse(null);
            currencyChoiceBox.setValue(item);
        }
        contrastToggle.setSelected(userConfig.getHighContrast());
        serverField.setText(userConfig.getUrl());
        confirmationLabel.setVisible(false);
        ft = new FadeTransition(Duration.millis(2000), confirmationLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(1000));
        ft.setOnFinished(e -> confirmationLabel.setVisible(false));
    }

    /**
     * Initializes the stage and lastContrast properties
     *
     * @param stage stage this scene is in
     */
    public void display(Stage stage) {
        this.stage = stage;
        lastContrast = userConfig.getHighContrast();
    }

    /**
     * Sets the contrast immediately
     */
    @FXML
    public void contrastClicked() {
        if (contrastToggle.isSelected())
            stage.getScene().getRoot().setEffect(getHighContrastEffect());
        else stage.getScene().getRoot().setEffect(null);
        userConfig.setHighContrast(contrastToggle.isSelected());
    }

    /**
     * Saves user selected settings in the config and informs the user
     */
    @FXML
    public void saveClicked() {
        String serverURL = serverField.getText();
        try {
            String currency = currencyChoiceBox.getValue().toString();
            if(currency.length() == 3) {
                userConfig.setCurrency(currency);
            }
            userConfig.persistContrast();
            userConfig.setURL(serverURL);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(languageConf.get("unexpectedError"));
            alert.setContentText(languageConf.get("UserConfig.IOError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
        }
        ft.stop();
        confirmationLabel.setText("Settings saved successfully!");
        confirmationLabel.setVisible(true);
        confirmationLabel.setOpacity(1.0);
        ft.play();
    }

    /**
     * Reset the fields and close the options
     */
    @FXML
    public void cancelClicked() {
        serverField.setText(userConfig.getUrl());
        String cur = userConfig.getCurrency();
        CommonFunctions.HideableItem<String> item =
                currencyChoiceBox.getItems().stream()
                        .filter(i -> i.toString().equals(cur)).findFirst().orElse(null);
        currencyChoiceBox.setValue(item);
        contrastToggle.setSelected(lastContrast);
        userConfig.setHighContrast(lastContrast);
        stage.close();
    }

    /**
     * Ping the server with the URL in the text field
     * and display a message based on the result
     */
    @FXML
    public void checkClicked() {
        serverField.setDisable(true);
        boolean result = server.ping(serverField.getText());
        serverField.setDisable(false);
        ft.stop();
        if(result) {
            confirmationLabel.setText("Server found successfully!");
        } else {
            confirmationLabel.setText("The URL might be incorrect or the server is down");
        }
        confirmationLabel.setVisible(true);
        confirmationLabel.setOpacity(1.0);
        ft.play();
    }
}
