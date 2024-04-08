package client.scenes;


import client.MyFXML;
import client.TestMainCtrl;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import commons.WebsocketActions;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
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
public class EditTitleCtrlTest {

    EditTitleCtrl ctrl;

    Scene scene;

    TestMainCtrl mainCtrl;

    Stage stage;
    TestServerUtils server;
    TestWebsocket websocket;

    LanguageConf languageConf;

    Event event;

    @BeforeAll
    static void setUp() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        mainCtrl = new TestMainCtrl();

        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));

        languageConf = new LanguageConf(userConfig);

        var editTitleLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/EditTitle.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EditTitleCtrl(mainCtrl, server, websocket, languageConf),
                StandardCharsets.UTF_8);


        scene = new Scene(editTitleLoader.load());
        ctrl = editTitleLoader.getController();
        event = new Event("test");
        event = server.createEvent(event);
        waitForFxEvents();
        stage.setScene(scene);


    }

    @Test
    public void testInitializeAndDisplayEditEventTitle(FxRobot robot) {
        Platform.runLater(() -> {
            ctrl.initialize();
            ctrl.displayEditEventTitle(event, stage);
            assertEquals(event.getTitle(), robot.lookup("#eventTitle").queryAs(Text.class).getText());
            assertFalse(robot.lookup("#warningLabel").queryAs(Label.class).isVisible());
            assertEquals("", robot.lookup("#nameTextField").queryAs(TextField.class).getText());
            assertEquals(languageConf.get("TitleChanger.pageTitle"), stage.getTitle());
            assertFalse(stage.isResizable());
        });
        waitForFxEvents();
    }

    @Test
    public void testSaveButton(FxRobot robot) {
        Platform.runLater(() -> {
            ctrl.initialize();
            ctrl.displayEditEventTitle(event, stage);
            robot.lookup("#nameTextField").queryAs(TextField.class).setText("newTitle");
            robot.clickOn("#saveButton");

        });
        waitForFxEvents();
        assertEquals("newTitle", event.getTitle());
        assertTrue(server.getCalls().contains("updateEventTitle"));
        assertEquals(server.getStatuses().get(1), 204);
        assertTrue(websocket.hasActionBeenTriggered(WebsocketActions.TITLE_CHANGE));
        assertTrue(websocket.hasPayloadBeenSent("newTitle"));
    }

    @Test
    public void testCancelButton(FxRobot robot) {
        Platform.runLater(() -> {
            server.getCalls().clear();
            websocket.resetTriggers();
            ctrl.initialize();
            ctrl.displayEditEventTitle(event, stage);
            robot.lookup("#nameTextField").queryAs(TextField.class).setText("newTitle");
            robot.clickOn("#cancelButton");

        });
        waitForFxEvents();
        assertEquals("test", event.getTitle());
        assertFalse(server.getCalls().contains("updateEventTitle"));
        assertEquals(0, server.getCalls().size());
        assertFalse(server.getStatuses().contains(204));
        assertFalse(websocket.hasActionBeenTriggered(WebsocketActions.TITLE_CHANGE));
        assertFalse(websocket.hasPayloadBeenSent("newTitle"));
    }

    @Test
    public void emptyTitleTest(FxRobot robot) {
        Platform.runLater(() -> {
            server.getCalls().clear();
            websocket.resetTriggers();
            ctrl.initialize();
            ctrl.displayEditEventTitle(event, stage);
            robot.clickOn("#saveButton");
        });
        waitForFxEvents();
        assertTrue(server.getCalls().contains("updateEventTitle"));
        assertTrue(server.getStatuses().contains(400));
        assertFalse(websocket.hasActionBeenTriggered(WebsocketActions.TITLE_CHANGE));
        assertFalse(websocket.hasPayloadBeenSent("newTitle"));
        assertTrue(robot.lookup("#warningLabel").queryAs(Label.class).isVisible());
    }

    @Test
    public void testLengthWarning(FxRobot robot) {
        Platform.runLater(() -> {
            server.getCalls().clear();
            websocket.resetTriggers();
            ctrl.initialize();
            ctrl.displayEditEventTitle(event, stage);
            String longTitle = "thisTitleIsLongerThan30Characters";
            robot.lookup("#nameTextField").queryAs(TextField.class).setText(longTitle);
            robot.clickOn("#saveButton");
            assertTrue(robot.lookup("#warningLabel").queryAs(Label.class).isVisible());
        });
        waitForFxEvents();
        assertEquals(event.getTitle(), "thisTitleIsLongerThan30Charact"); // first 30 characters

    }
}
