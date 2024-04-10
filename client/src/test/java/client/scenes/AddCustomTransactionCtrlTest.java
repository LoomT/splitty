package client.scenes;

import client.MyFXML;
import client.TestMainCtrl;
import client.utils.CommonFunctions;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import commons.Event;
import commons.Participant;
import commons.Transaction;
import commons.WebsocketActions;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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

@SuppressWarnings("ALL")
@ExtendWith(ApplicationExtension.class)
class AddCustomTransactionCtrlTest {

    AddCustomTransactionCtrl ctrl;
    TestServerUtils server;
    TestWebsocket websocket;
    Stage stage;
    Event event;
    FileManagerMock fileManager;
    CurrencyConverter converter;

    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        fileManager = new FileManagerMock();
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=localhost:8080
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        converter = new CurrencyConverter(server, fileManager, languageConf);

        var addCustomTransactionLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/AddCustomTransaction.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AddCustomTransactionCtrl(new TestMainCtrl(), server, languageConf,
                        converter, userConfig), StandardCharsets.UTF_8);
        Scene scene = new Scene(addCustomTransactionLoader.load());
        ctrl = addCustomTransactionLoader.getController();

        stage.setScene(scene);
        stage.show();
        event = new Event("title");
        event = server.createEvent(event);
        Participant p1 = new Participant("bob");
        Participant p2 = new Participant("tom");
        server.createParticipant(event.getId(), p1);
        server.createParticipant(event.getId(), p2);
        event = server.getEvent(event.getId());
        websocket.on(WebsocketActions.ADD_TRANSACTION, (t) -> event.addTransaction((Transaction) t));
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
    void display(FxRobot robot) {
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiver").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiver").query();
        ComboBox<CommonFunctions.HideableItem<String>> currencies = robot.fromAll().lookup("#chooseCurrency").query();
        assertEquals(2, givers.getItems().size());
        assertEquals(2, receivers.getItems().size());
        assertEquals(fileManager.getAvailableCurrencies().size(), currencies.getItems().size());
    }

    @Test
    void saveClicked(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).setValue(new CommonFunctions.HideableItem<>("USD"));
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("addTransaction"));
        assertFalse(event.getTransactions().isEmpty());
        Transaction saved = event.getTransactions().getFirst();
        Transaction expected = new Transaction(event.getParticipants().getFirst(),
                event.getParticipants().get(1), 1, "USD", saved.getDate());
        expected.setEventID(event.getId());
        assertNotEquals(0, saved.getId());
        saved.setId(0);
        assertEquals(expected, saved);
        assertFalse(stage.isShowing());
    }

    @Test
    void saveClickedNotBaseCurrency(FxRobot robot) throws IOException {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).setValue(new CommonFunctions.HideableItem<>("EUR"));
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("addTransaction"));
        Transaction saved = event.getTransactions().getFirst();
        double expected = converter.convert("EUR", "USD",  1, saved.getDate().toInstant());
        assertEquals(expected, saved.getAmount());
    }

    @Test
    void saveClickedSameParticipant(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedNoAmount(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedDefaultCurrency(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("addTransaction"));
        assertFalse(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedNoGiver(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedNoReceiver(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedBrokenCurrency(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).setValue(null);
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedInputLettersIntoAmount(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).setValue(new CommonFunctions.HideableItem<>("USD"));
            robot.lookup("#amountField").queryAs(TextField.class).setText("AB");
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            ctrl.saveClicked();
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("addTransaction"));
        assertFalse(event.getTransactions().isEmpty());
        assertEquals(1, event.getTransactions().getFirst().getAmount());
    }

    @Test
    void saveClickedInputZero(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        Platform.runLater(() -> {
            ctrl.display(event, stage);
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#amountField").queryAs(TextField.class).setText("0");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedWhileParticipantChanged(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        event.getParticipants().removeFirst();
        Platform.runLater(() -> {
            robot.lookup("#chooseGiver").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#chooseReceiver").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#chooseCurrency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#amountField").queryAs(TextField.class).setText("1");
            robot.lookup("#save").queryButton().fire();
        });
        waitForFxEvents();
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void backClicked() {
        ctrl.display(event, stage);
        assertTrue(stage.isShowing());
        Platform.runLater(() -> {
            ctrl.backClicked();
        });
        waitForFxEvents();
        assertFalse(stage.isShowing());
    }
}