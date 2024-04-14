package client.scenes;


import client.MyFXML;
import client.TestMainCtrl;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class StartScreenCtrlTest {
    StartScreenCtrl startScreenCtrl;

    TestServerUtils server;
    Scene scene;
    TestWebsocket websocket;

    UserConfig userConfig;

    @Start
    public void start(Stage stage) throws IOException {
        websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        userConfig = new UserConfig(new TestIO("""
                serverURL=localhost:8080
                lang=en
                recentEventCodes=
                locales=["en", "nl"]"""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        TestMainCtrl mainCtrl = new TestMainCtrl();

        var startScreenPageLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/StartScreen.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new StartScreenCtrl(server, mainCtrl, languageConf, userConfig),
                StandardCharsets.UTF_8);
        scene = new Scene(startScreenPageLoader.load());
        startScreenCtrl = startScreenPageLoader.getController();
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
    void initializeTest(FxRobot robot){
        assertFalse(robot.lookup("#joinError").queryAs(Label.class).isVisible());
        assertFalse(robot.lookup("#createEventError").queryAs(Label.class).isVisible());
        assertTrue(robot.lookup("#eventList").queryAs(VBox.class).getChildren().isEmpty());
    }

    @Test
    void createEventTest(FxRobot robot){
        Platform.runLater(() -> {
            robot.lookup("#title").queryAs(TextField.class).setText("TestEvent");
            robot.clickOn("#createButton");
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("createEvent"));
        assertTrue(server.getStatuses().contains(200));
    }

    @Test
    void joinEventTest(FxRobot robot){
        Platform.runLater(() -> {
            Event event = new Event("TestEvent");
            event = server.createEvent(event);
            event.setId("ABCDE");
            String code = event.getId();
            robot.lookup("#code").queryAs(TextField.class).setText(code);
            robot.clickOn("#joinButton");
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("getEvent"));
        assertTrue(server.getStatuses().contains(200));

    }

    @Test
    void testReloadEventCodes(FxRobot robot){
        Platform.runLater(() -> {
            Event event1 = server.createEvent(new Event("TestEvent"));
            Event event2 = server.createEvent(new Event("TestEvent2"));
            event1.setId("ABCDE");
            event2.setId("FGHIJ");
            userConfig.setMostRecentEventCode(event1.getId());
            userConfig.setMostRecentEventCode(event2.getId());
            startScreenCtrl.reloadEventCodes();
        });
        waitForFxEvents();
        assertEquals(2, robot.lookup("#eventList").queryAs(VBox.class).getChildren().size());
    }

    @Test
    void joinNonExistingEvent(FxRobot robot){
        Platform.runLater(() -> {
            server.getCalls().clear();
            robot.lookup("#code").queryAs(TextField.class).setText("KIZHL");
            robot.clickOn("#joinButton");
        });
        waitForFxEvents();
        assertTrue(robot.lookup("#joinError").queryAs(Label.class).isVisible());
        assertTrue(server.getCalls().contains("getEvent"));
    }


}
