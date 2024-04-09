package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

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
    Stage stage;
    FadeTransition ft;
    boolean lastContrast = false;

    @Inject
    public OptionsCtrl(UserConfig userConfig, LanguageConf languageConf,
                       CurrencyConverter converter, ServerUtils server) {
        this.userConfig = userConfig;
        this.languageConf = languageConf;
        this.converter = converter;
        this.server = server;
    }

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

    public void display(Stage stage) {
        this.stage = stage;
        lastContrast = userConfig.getHighContrast();
    }

    @FXML
    public void contrastClicked() {
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(-0.4);
        ca.setContrast(1);

        Blend b = new Blend();
        b.setMode(BlendMode.COLOR_BURN);
        b.setOpacity(.8);




        b.setTopInput(ca);
//            ca.setInput(b);

        if (contrastToggle.isSelected()) stage.getScene().getRoot().setEffect(b);
        else stage.getScene().getRoot().setEffect(null);
        userConfig.setHighContrast(contrastToggle.isSelected());
    }

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

    @FXML
    public void cancelClicked() {
        serverField.setText(userConfig.getUrl());
        String cur = userConfig.getCurrency();
        CommonFunctions.HideableItem<String> item =
                currencyChoiceBox.getItems().stream()
                        .filter(i -> i.toString().equals(cur)).findFirst().orElse(null);
        currencyChoiceBox.setValue(item);
        contrastToggle.setSelected(lastContrast);
        stage.close();
    }

    @FXML
    public void checkClicked() {
        serverField.setDisable(true);
        boolean result = server.ping(serverField.getText());
        serverField.setDisable(false);
        ft.stop();
        if(result) {
            confirmationLabel.setText("Server is up!");
        } else {
            confirmationLabel.setText("Server is down!");
        }
        confirmationLabel.setVisible(true);
        confirmationLabel.setOpacity(1.0);
        ft.play();
    }
}
