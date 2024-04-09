package client.scenes;

import client.MyFXML;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import commons.Participant;
import commons.WebsocketActions;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class EditparticipantsCtrlTest {
    EditParticipantsCtrl ctrl;
    TestServerUtils server;
    Scene scene;
    TestWebsocket websocket;

    @Start
    public void start(Stage stage) throws IOException {
        server = new TestServerUtils();
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(null, languageConf, userConfig);
        websocket = new TestWebsocket();

        var editParticipantsPageLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/EditParticipants.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EditParticipantsCtrl(server, mainCtrl, languageConf, websocket),
                StandardCharsets.UTF_8);
        scene = new Scene(editParticipantsPageLoader.load());
        ctrl = editParticipantsPageLoader.getController();

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
    public void initializeTest(FxRobot robot) {
        Platform.runLater(() -> {
            ctrl.initialize();
            ctrl.displayEditParticipantsPage(new Event("old title"));
            websocket.simulateAction(WebsocketActions.TITLE_CHANGE, "new title");
            assertEquals("new title", ctrl.getEvent().getTitle());
        });
    }

    @Test
    public void displayEditParticipantsPageTest() {
        Platform.runLater(()->{
            Event e = new Event("new event");
            Event e2 = new Event("new event");
            Participant p = new Participant("name");
            e2.getParticipants().add(p);
            ctrl.displayEditParticipantsPage(e);
            websocket.simulateAction(WebsocketActions.ADD_PARTICIPANT, p);
            assertEquals(e2, ctrl.getEvent());
        });
    }

    @Test
    public void saveButtonClickedTest(FxRobot robot) {
        Platform.runLater(()->{
            Event e = server.createEvent(new Event("testEvent"));
            ctrl.displayEditParticipantsPage(e);
            robot.sleep(200);
            robot.lookup("#nameTextField").queryAs(TextField.class).setText("name2");
            System.out.println(websocket.getFunctions());
            robot.clickOn("#saveButton");
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println(server.getEvents());
        });
    }
}
