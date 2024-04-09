package client.scenes;

import client.MyFXML;
import client.components.EventListItemAdmin;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyCode;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class AdminOverviewCtrlTest {
    AdminOverviewCtrl ctrl;
    TestServerUtils server;
    Scene scene;
    @Start
    public void start(Stage stage) throws IOException {
        server = new TestServerUtils(new TestWebsocket());
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(null, languageConf, userConfig, null);

        var adminOverviewLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/AdminOverview.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AdminOverviewCtrl(server, mainCtrl, userConfig, languageConf),
                StandardCharsets.UTF_8);
        scene = new Scene(adminOverviewLoader.load());
        ctrl = adminOverviewLoader.getController();

        ctrl.setPassword("password");
        ctrl.initPoller(500L); // 0.5 sec time out
        ctrl.loadAllEvents(); // the password needs to be set before this method
        stage.setTitle(languageConf.get("AdminOverview.title"));
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
    void refreshNotClicked(FxRobot robot) {
        assertEquals(server.getStatuses().getFirst(), 200);
        assertEquals(server.getStatuses().size(), 1);
    }
    @Test
    void refreshClicked(FxRobot robot) {
        robot.clickOn("#refreshBtn");
        assertEquals(server.getStatuses().getFirst(), 200);
        assertEquals(server.getStatuses().get(1), 200);
    }

    @Test
    void autoRefresh(FxRobot robot) throws InterruptedException {
        assertEquals(0, robot.fromAll().lookup(".eventListItemContainer").queryAll().size());
        server.createEvent(new Event("title"));
        Thread.sleep(1000);
        assertEquals(1, robot.fromAll().lookup(".eventListItemContainer").queryAll().size());
    }
    @Test
    void reverseOrder(FxRobot robot) throws InterruptedException {
        server.createEvent(new Event("old"));
        Thread.sleep(50);
        server.createEvent(new Event("new"));
        robot.clickOn("#refreshBtn");
        List<String> inOrder = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("2", "1"), inOrder);
        robot.clickOn("#reverseBtn");
        List<String> reverseOrder = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("1", "2"), reverseOrder);
    }

    @Test
    void creationDateOrder(FxRobot robot) throws InterruptedException {
        server.createEvent(new Event("old"));
        Thread.sleep(50);
        server.createEvent(new Event("new"));
        robot.clickOn("#refreshBtn");
        List<String> inOrder = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("2", "1"), inOrder);
    }

    @Test
    void lastActivityOrder(FxRobot robot) throws InterruptedException {
        Event event = server.createEvent(new Event("old"));
        Thread.sleep(50);
        server.createEvent(new Event("new"));
        Thread.sleep(50);
        server.createParticipant(event.getId(), new Participant("Bob"));
        robot.clickOn("#refreshBtn");
        List<String> inCreation = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("2", "1"), inCreation);
        ChoiceBox<String> choiceBox = robot.fromAll().lookup("#orderDropdownBtn").query();
        robot.clickOn(choiceBox);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        List<String> inActivity = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("1", "2"), inActivity);
    }

    @Test
    void eventNameOrder(FxRobot robot) throws InterruptedException {
        Event event = server.createEvent(new Event("1"));
        Thread.sleep(50);
        server.createEvent(new Event("2"));
        server.createParticipant(event.getId(), new Participant("Bob"));
        robot.clickOn("#refreshBtn");
        List<String> inCreation = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("2", "1"), inCreation);
        ChoiceBox<String> choiceBox = robot.fromAll().lookup("#orderDropdownBtn").query();
        robot.clickOn(choiceBox);
        robot.type(KeyCode.DOWN).type(KeyCode.ENTER);
        List<String> inActivity = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("1", "2"), inActivity);
    }

    @Test
    void participantCountOrder(FxRobot robot) throws InterruptedException {
        Event event = server.createEvent(new Event("1"));
        Thread.sleep(50);
        server.createEvent(new Event("2"));
        server.createParticipant(event.getId(), new Participant("Bob"));
        robot.clickOn("#refreshBtn");
        List<String> inCreation = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("2", "1"), inCreation);
        ChoiceBox<String> choiceBox = robot.fromAll().lookup("#orderDropdownBtn").query();
        robot.clickOn(choiceBox);
        robot.type(KeyCode.DOWN).type(KeyCode.DOWN).type(KeyCode.ENTER);
        List<String> inActivity = robot.fromAll().lookup(".eventListItemContainer")
                .queryAll().stream().map(e -> ((EventListItemAdmin) e)
                        .getEventCodeLabel().getText()).toList();
        assertEquals(List.of("1", "2"), inActivity);
    }

    // not sure how to test concurrency
//    @Test
//    void initPollerTimeOut() throws InterruptedException {
//        Thread.sleep(200);
//        assertTrue(server.isPolled());
//        assertTrue(server.getConcurrentStatuses().contains(408));
//    }
//
//    @Test
//    void initPollerResponse(FxRobot robot) throws InterruptedException {
//        Thread.sleep(200);
//        server.createEvent(new Event("title"));
//        Thread.sleep(500);
//        System.out.println(server.getConcurrentStatuses());
//        assertTrue(server.isPolled());
//        assertNotEquals(0, robot.fromAll().lookup(".eventListItemContainer").queryAll().size());
//    }
//
//    @Test
//    void initPollerIncorrectPassword(FxRobot robot) throws InterruptedException {
//        ctrl.setPassword("forgor");
//        Thread.sleep(600);
//        server.createEvent(new Event("title"));
//        Thread.sleep(600);
//        assertTrue(server.isPolled());
//        assertTrue(server.getConcurrentStatuses().contains(401));
//        assertFalse(server.getConcurrentStatuses().contains(204));
//        assertEquals(0, robot.fromAll().lookup(".eventListItemContainer").queryAll().size());
//    }
//
//    @Test
//    void stopPoller(FxRobot robot) throws InterruptedException {
//        ctrl.stopPoller();
//        Thread.sleep(600);
//        server.createEvent(new Event("title"));
//        Thread.sleep(300);
//        assertTrue(server.isPolled());
//        assertEquals(0, robot.fromAll().lookup(".eventListItemContainer").queryAll().size());
//    }
}
