package client.scenes;

import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
    @FXML
    private ProgressIndicator loadIndicator;
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
        contrastToggle.setText(userConfig.getHighContrast() ?
                languageConf.get("Options.on") : languageConf.get("Options.off"));
        serverField.setText(userConfig.getUrl());
        confirmationLabel.setVisible(false);
        ft = new FadeTransition(Duration.millis(2000), confirmationLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(1000));
        ft.setOnFinished(e -> confirmationLabel.setVisible(false));
        loadIndicator.setVisible(false);
    }

    /**
     * Initializes the stage and lastContrast properties
     *
     * @param stage stage this scene is in
     */
    public void display(Stage stage) {
        this.stage = stage;
        stage.getScene().getWindow()
                .addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> cancelClicked());
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
        contrastToggle.setText(userConfig.getHighContrast() ?
                languageConf.get("Options.on") : languageConf.get("Options.off"));
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
            lastContrast = userConfig.getHighContrast();
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
        confirmationLabel.setText(languageConf.get("Options.saved"));
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
        contrastToggle.setText(userConfig.getHighContrast() ?
                languageConf.get("Options.on") : languageConf.get("Options.off"));
        userConfig.setHighContrast(lastContrast);
        stage.close();
    }

    /**
     * Ping the server with the URL in the text field
     * and display a message based on the result
     */
    @FXML
    public void checkClicked() {
        loadIndicator.setVisible(true);
        serverField.setDisable(true);
        confirmationLabel.setVisible(false);
        boolean result = server.ping(serverField.getText());
        serverField.setDisable(false);
        loadIndicator.setVisible(false);
        ft.stop();
        if(result) {
            confirmationLabel.setText(languageConf.get("Options.serverUp"));
        } else {
            confirmationLabel.setText(languageConf.get("Options.serverDown"));
        }
        confirmationLabel.setVisible(true);
        confirmationLabel.setOpacity(1.0);
        ft.play();
    }

    private final BooleanProperty ctrlPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty sPressed = new SimpleBooleanProperty(false);
    private final BooleanBinding spaceAndRightPressed = ctrlPressed.and(sPressed);
    /**
     * Enable keyboard shortcuts
     *
     * @param scene this options scene
     */
    public void initializeShortcuts(Scene scene) {
        MainCtrl.checkKey(scene, this::cancelClicked, KeyCode.ESCAPE);
        MainCtrl.checkKey(scene, () -> this.currencyChoiceBox.show(),
                currencyChoiceBox, KeyCode.ENTER);


        // adds a listener for when CTRL + S is pressed to save settings
        spaceAndRightPressed.addListener((observable, oldValue, newValue) -> saveClicked());

        scene.setOnKeyPressed(key -> {
            if (key.getCode() == KeyCode.CONTROL) {
                ctrlPressed.set(true);
            } else if (key.getCode() == KeyCode.S) {
                sPressed.set(true);
            }
        });

        scene.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.CONTROL) {
                ctrlPressed.set(false);
            } else if (key.getCode() == KeyCode.S) {
                sPressed.set(false);
            }
        });
    }
}
