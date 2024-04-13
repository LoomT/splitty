package client.scenes;

import client.MyFXML;
import client.utils.CommonFunctions;
import client.utils.EmailService;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import utils.FileManagerMock;
import utils.TestIO;
import utils.TestServerUtils;
import utils.TestWebsocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
class OptionsCtrlTest {

    private Stage stage;
    private TestServerUtils server;
    private OptionsCtrl ctrl;
    private UserConfig userConfig;
    private TestIO testIO;
    private int reloadedFXML;

    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        TestWebsocket websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        testIO = new TestIO("""
                serverURL=localhost:8080
                lang=en
                recentEventCodes=
                currency=EUR
                highContrast=false
                """);

        userConfig = new UserConfig(testIO);

        LanguageConf languageConf = new LanguageConf(userConfig);

        var optionsLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/Options.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new OptionsCtrl(userConfig, languageConf, new CurrencyConverter(
                        server, new FileManagerMock(), languageConf), server,
                        new EmailService(userConfig, languageConf)),
                StandardCharsets.UTF_8);

        reloadedFXML = 0;
        userConfig.onContrastChange(() -> reloadedFXML++);
        Scene scene = new Scene(optionsLoader.load());
        ctrl = optionsLoader.getController();
        stage.setScene(scene);
        ctrl.display(stage);
        stage.show();
        waitForFxEvents();
    }

    @BeforeAll
    static void setUp() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void contrastClicked(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#contrastToggle").queryAs(ToggleButton.class).fire();
        });
        waitForFxEvents();
        assertEquals("ON", robot.lookup("#contrastToggle").queryAs(ToggleButton.class).getText());
        assertTrue(userConfig.getHighContrast());
    }

    @Test
    void contrastClickedNotSaved(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#contrastToggle").queryAs(ToggleButton.class).fire();
        });
        waitForFxEvents();
        assertTrue(testIO.getContent().contains("highContrast=false"));
        assertEquals(0, testIO.getWrites());
    }

    @Test
    void contrastClickedReloadFXML(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#contrastToggle").queryAs(ToggleButton.class).fire();
        });
        waitForFxEvents();
        assertEquals(1, reloadedFXML);
        Platform.runLater(() -> {
            robot.lookup("#contrastToggle").queryAs(ToggleButton.class).fire();
        });
        waitForFxEvents();
        assertEquals(2, reloadedFXML);
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveClicked(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#contrastToggle").queryAs(ToggleButton.class).fire();
            robot.lookup("#serverField").queryAs(TextField.class).setText("coolmathgames");
            robot.lookup("#emailUsername").queryAs(TextField.class).setText("paulatreides10191@gmail.com");
            robot.lookup("#emailPassword").queryAs(TextField.class).setText("LisanAlGaib123");
            ComboBox<CommonFunctions.HideableItem<String>> selection = robot.lookup("#currencyChoiceBox").queryAs(ComboBox.class);
            selection.setValue(selection.getItems().get(1)); // GBP
            ctrl.saveClicked();
        });
        waitForFxEvents();
        assertEquals(5, testIO.getWrites());
        assertEquals("coolmathgames", userConfig.getUrl());
        assertEquals("GBP", userConfig.getCurrency());
        assertEquals("paulatreides10191@gmail.com", userConfig.getUsername());
        assertEquals("LisanAlGaib123", userConfig.getMailPassword());
        assertTrue(userConfig.getHighContrast());
        assertTrue(userConfig.getHighContrast());
    }

    @Test
    void cancelClicked() {
        Platform.runLater(() -> {
            ctrl.cancelClicked();
        });
        waitForFxEvents();
        assertFalse(stage.isShowing());
    }

    @Test
    void cancelClickedAfterContrast(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#contrastToggle").queryAs(ToggleButton.class).fire();
        });
        waitForFxEvents();
        Platform.runLater(() -> {
            assertTrue(robot.lookup("#contrastToggle").queryAs(ToggleButton.class).isSelected());
            ctrl.cancelClicked();
        });
        waitForFxEvents();
        assertFalse(userConfig.getHighContrast());
    }

    @Test
    void cancelClickedAfterServer(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#serverField").queryAs(TextField.class).setText("coolmathgames");
        });
        waitForFxEvents();
        Platform.runLater(() -> {
            assertEquals("coolmathgames", robot.lookup("#serverField").queryAs(TextField.class).getText());
            ctrl.cancelClicked();
        });
        waitForFxEvents();
        assertEquals("localhost:8080", userConfig.getUrl());
    }

    @SuppressWarnings("unchecked")
    @Test
    void cancelClickedAfterCurrency(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#currencyChoiceBox").queryAs(ComboBox.class)
                    .setValue(robot.lookup("#currencyChoiceBox").queryAs(ComboBox.class).getItems().get(1)); // GBP
        });
        waitForFxEvents();
        Platform.runLater(() -> {
            assertEquals("GBP", robot.lookup("#currencyChoiceBox").queryAs(ComboBox.class).getValue().toString());
            ctrl.cancelClicked();
        });
        waitForFxEvents();
        assertEquals("EUR", userConfig.getCurrency());
    }

    @Test
    void checkClicked() {
        Platform.runLater(() -> {
            ctrl.checkClicked();
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("ping"));
        assertTrue(server.isGoodPing());
    }

    @Test
    void checkClickedBad(FxRobot robot) {
        Platform.runLater(() -> {
            robot.lookup("#serverField").queryAs(TextField.class).setText("coolmathgames");
            ctrl.checkClicked();
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("ping"));
        assertFalse(server.isGoodPing());
    }
}