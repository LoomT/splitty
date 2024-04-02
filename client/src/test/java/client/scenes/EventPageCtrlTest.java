package client.scenes;

import client.MyFXML;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
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
import utils.TestIO;
import utils.TestServerUtils;
import utils.TestWebsocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class EventPageCtrlTest {

    EventPageCtrl ctrl;
    TestServerUtils server;

    @Start
    public void start(Stage stage) throws IOException {
        server = new TestServerUtils();

        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        Websocket websocket = new TestWebsocket();
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(null, languageConf, userConfig);

        var eventPageLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/EventPage.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EventPageCtrl(mainCtrl, languageConf, websocket, server),
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
                new Runnable() {
                    @Override
                    public void run() {
                        ctrl.displayEvent(e);
                        assertEquals(e, ctrl.getEvent());
                    }
                }
        );
    }

    @Test
    public void createExpensesTest(FxRobot robot) {
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), "food");
        Event e = new Event("test", List.of(p), List.of(ex));
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = new ListView();
                        ctrl.createExpenses(List.of(ex), lv, e);
                        assertEquals(1, lv.getItems().size());
                    }
                }
        );
    }

    @Test
    public void toStringText(FxRobot robot) {
        Participant p = new Participant("name");
        Expense ex = new Expense(p, "expense", 20d, "EUR", List.of(p), "food");
        ex.setDate(new Date("January 2, 2024"));
        Event e = new Event("test", List.of(p), List.of(ex));
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        NumberFormat currencyFormatter = switch (ex.getCurrency()) {
                            case "USD" -> NumberFormat.getCurrencyInstance(Locale.US);
                            case "EUR" -> NumberFormat.getCurrencyInstance(Locale.GERMANY);
                            case "GBP" -> NumberFormat.getCurrencyInstance(Locale.UK);
                            case "JPY" -> NumberFormat.getCurrencyInstance(Locale.JAPAN);
                            default -> NumberFormat.getCurrencyInstance(Locale.getDefault());
                        };

                        String formattedAmount = currencyFormatter.format(ex.getAmount());
                        assertEquals("2.1.2024     name paid " + formattedAmount + " for expense", ctrl.toString(ex));
                    }
                }
        );
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
