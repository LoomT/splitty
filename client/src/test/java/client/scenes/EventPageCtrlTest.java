package client.scenes;

import client.MyFXML;
import client.utils.EmailService;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import client.utils.currency.CurrencyConverter;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
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
import java.net.ConnectException;
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
    CurrencyConverter converter;

    @Start
    public void start(Stage stage) throws IOException {
        websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        fileManager = new FileManagerMock();

        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=localhost:8080
                lang=en
                recentEventCodes=
                currency=EUR
                locales=["en", "nl"]"""));
        Websocket websocket = new TestWebsocket();
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(null, languageConf, userConfig);
        converter = new CurrencyConverter(server, fileManager, languageConf);

        var eventPageLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/EventPage.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EventPageCtrl(mainCtrl, languageConf, websocket, server,
                        converter, userConfig, new EmailService(userConfig, languageConf)),
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
        Tag tag = new Tag("food", "#00FF00");
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), tag);
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
    public void toStringText(FxRobot robot) throws ParseException, CurrencyConverter.CurrencyConversionException, ConnectException {
        Tag tag = new Tag("food", "#00FF00");
        Participant p = new Participant("name");
        double amount = converter.convert("EUR", "USD", 20,
                new SimpleDateFormat("MM/dd/yy").parse("01/02/2024").toInstant());
        Expense ex = new Expense(p, "expense", amount, "EUR", List.of(p), tag);
        ex.setDate(new SimpleDateFormat("MM/dd/yy").parse("01/02/2024"));
        assertEquals("2024-01-02     name paid \u20ac10.00 for expense", ctrl.toString(ex));
        assertTrue(server.getCalls().contains("getExchangeRates"));
    }

    @Test
    public void getExpensesFromTest(FxRobot robot) {
        Tag tag = new Tag("food", "#00FF00");
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), tag);
        Event e = new Event("test", List.of(p), List.of(ex));

        assertEquals(List.of(ex), ctrl.getExpensesFrom(e, "name"));

    }

    @Test
    public void getExpensesIncludingTest(FxRobot robot) {
        Tag tag = new Tag("food", "#00FF00");
        Participant p = new Participant("name");
        Participant p2 = new Participant("name2");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), tag);
        Event e = new Event("test", List.of(p, p2), List.of(ex));

        assertEquals(List.of(), ctrl.getExpensesIncluding(e, "name2"));
        assertEquals(List.of(ex), ctrl.getExpensesIncluding(e, "name"));

    }
}
