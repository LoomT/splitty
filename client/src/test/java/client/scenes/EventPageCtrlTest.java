package client.scenes;

import client.MyFXML;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import client.utils.currency.CurrencyConverter;
import commons.Event;
import commons.Expense;
import commons.Participant;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class EventPageCtrlTest {

    EventPageCtrl ctrl;
    TestServerUtils server;
    FileManagerMock fileManager;
    TestWebsocket websocket;

    @Start
    public void start(Stage stage) throws IOException {
        websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        fileManager = new FileManagerMock();

        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes=
                currency=EUR"""));
        Websocket websocket = new TestWebsocket();
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(null, languageConf, userConfig, null);

        var eventPageLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/EventPage.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EventPageCtrl(mainCtrl, languageConf, websocket, server,
                        new CurrencyConverter(server, fileManager, languageConf), userConfig),
                StandardCharsets.UTF_8);
        Scene scene = new Scene(eventPageLoader.load());
        ctrl = eventPageLoader.getController();

        stage.setScene(scene);
        stage.show();
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
    public void testDisplayEvent(FxRobot robot) {
        Event e = new Event("test", List.of(new Participant("name")), List.of());
        Platform.runLater(
                () -> {
                    ctrl.displayEvent(e);
                    assertEquals(e, ctrl.getEvent());
                }
        );
    }

    @Test
    public void createExpensesTest(FxRobot robot) {
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), "food");
        Event e = new Event("test", List.of(p), List.of(ex));
        Platform.runLater(
                () -> {
                    ListView<String> lv = new ListView<>();
                    ctrl.createExpenses(List.of(ex), lv, e);
                    assertEquals(1, lv.getItems().size());
                }
        );
    }

    @Test
    public void toStringText(FxRobot robot) throws ParseException {
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), "food");
        ex.setDate(new SimpleDateFormat("MM/dd/yy").parse("01/02/2024"));
        assertEquals("2024-01-02     name paid \u20ac20.00 for expense", ctrl.toString(ex));
        assertTrue(server.getCalls().contains("getExchangeRates"));
    }

    @Test
    public void getExpensesFromTest(FxRobot robot) {
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), "food");
        Event e = new Event("test", List.of(p), List.of(ex));

        assertEquals(List.of(ex), ctrl.getExpensesFrom(e, "name"));

    }

    @Test
    public void getExpensesIncludingTest(FxRobot robot) {
        Participant p = new Participant("name");
        Participant p2 = new Participant("name2");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), "food");
        Event e = new Event("test", List.of(p, p2), List.of(ex));

        assertEquals(List.of(), ctrl.getExpensesIncluding(e, "name2"));
        assertEquals(List.of(ex), ctrl.getExpensesIncluding(e, "name"));

    }
}
