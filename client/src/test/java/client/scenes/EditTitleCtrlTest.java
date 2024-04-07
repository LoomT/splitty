package client.scenes;


import client.MyFXML;
import client.TestMainCtrl;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import commons.Event;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
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


    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        server = new TestServerUtils();
        mainCtrl = new TestMainCtrl();

        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));

        LanguageConf languageConf = new LanguageConf(userConfig);

        var editTitleLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/EditTitle.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EditTitleCtrl(mainCtrl, server, new TestWebsocket(), languageConf),
                StandardCharsets.UTF_8);


        scene = new Scene(editTitleLoader.load());
        ctrl = editTitleLoader.getController();
        Event event = new Event("test");
        ctrl.setEvent(event);
        server.createEvent(event);
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
    public void testDisplayEditTitle(FxRobot robot) {
        Platform.runLater(() -> {
            mainCtrl.showEditTitle(new Event("test"));
            assertEquals(mainCtrl.getScenes().getFirst(), "EditTitle");
        });
        waitForFxEvents();
    }


    @Test
    public void testFXMLItems(FxRobot robot) {
        assertNotNull(robot.lookup("#nameTextField").query());
        assertNotNull(robot.lookup("#saveButton").query());
        assertNotNull(robot.lookup("#eventTitle").query());
        assertNotNull(robot.lookup("#titleError").query());
    }

    @Test
    public void testSaveNewTitle(FxRobot robot) {
        String newTitle = "new title";
        robot.lookup("#nameTextField").queryAs(TextField.class).setText(newTitle);
        robot.clickOn("#saveButton");
        assertEquals("new title", ctrl.getEvent().getTitle());

    }

    @Test
    public void testCancelChangeTitle(FxRobot robot) {
        String newTitle = "new title";
        robot.lookup("#nameTextField").queryAs(TextField.class).setText(newTitle);
        robot.clickOn("#cancelButton");
        assertEquals("test", ctrl.getEvent().getTitle());
    }

    @Test
    public void testCharacterLimitError(FxRobot robot) {
        ctrl.initialize();
        String newTitle = "new title";
        robot.lookup("#nameTextField").queryAs(TextField.class).setText(newTitle.repeat(100));
        robot.clickOn("#saveButton");
        assertTrue(robot.lookup("#titleError").queryAs(Text.class).isVisible());
        assertEquals("test", ctrl.getEvent().getTitle());
    }

    @Test
    public void testDisplayEditTitlePage(FxRobot robot) {
        Platform.runLater(() -> {
            ctrl.displayEditEventTitle(new Event("test"), new Stage());
            assertEquals("test", ctrl.getEvent().getTitle());
            ctrl.cancelTitle();
            assertEquals("test", ctrl.getEvent().getTitle());
        });
        waitForFxEvents();
    }





}
