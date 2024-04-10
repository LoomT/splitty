package client.scenes;

import client.MyFXML;
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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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

@ExtendWith(ApplicationExtension.class)
class AddCustomTransactionCtrlTest {

    AddCustomTransactionCtrl ctrl;
    TestServerUtils server;
    TestWebsocket websocket;
    Stage stage;
    Event event;
    FileManagerMock fileManager;

    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        websocket = new TestWebsocket();

        server = new TestServerUtils(websocket);
        fileManager = new FileManagerMock();
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(websocket, languageConf, userConfig, null);

        var addCustomTransactionLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/AddCustomTransaction.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AddCustomTransactionCtrl(mainCtrl, server, languageConf),
                StandardCharsets.UTF_8);
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
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        assertEquals(2, givers.getItems().size());
        assertEquals(2, receivers.getItems().size());
        assertEquals(4, currencies.getItems().size());
    }

    @Test
    void saveClicked(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertTrue(server.getCalls().contains("addTransaction"));
        assertFalse(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedSameParticipant(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedNoAmount(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedDefaultCurrency(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertTrue(server.getCalls().contains("addTransaction"));
        assertFalse(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedNoGiver(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedNoReceiver(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedBrokenCurrency(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        Platform.runLater(() -> currencies.setValue(null));
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedInputLettersIntoAmount(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.A).type(KeyCode.B).type(KeyCode.DIGIT1);
        robot.clickOn(save);
        assertTrue(server.getCalls().contains("addTransaction"));
        assertFalse(event.getTransactions().isEmpty());
        assertEquals(1, event.getTransactions().getFirst().getAmount());
    }

    @Test
    void saveClickedInputZero(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT0);
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void saveClickedWhileParticipantChanged(FxRobot robot) {
        assertTrue(event.getTransactions().isEmpty());
        ctrl.display(event, stage);
        ChoiceBox<String> givers = robot.fromAll().lookup("#chooseGiverBtn").query();
        ChoiceBox<String> receivers = robot.fromAll().lookup("#chooseReceiverBtn").query();
        ChoiceBox<String> currencies = robot.fromAll().lookup("#chooseCurrencyBtn").query();
        TextField amountField = robot.fromAll().lookup("#chooseAmountBtn").query();
        Button save = robot.fromAll().lookup("#saveBtn").queryButton();
        robot.clickOn(givers);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(receivers);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(currencies);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        robot.clickOn(amountField);
        robot.type(KeyCode.DIGIT1);
        event.getParticipants().removeFirst();
        robot.clickOn(save);
        assertFalse(server.getCalls().contains("addTransaction"));
        assertTrue(event.getTransactions().isEmpty());
    }

    @Test
    void backClicked() {
        ctrl.display(event, stage);
        assertTrue(stage.isShowing());
        Platform.runLater(() -> {
            ctrl.backClicked();
            assertFalse(stage.isShowing());
        });
    }
}