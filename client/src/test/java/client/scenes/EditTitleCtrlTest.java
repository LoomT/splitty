package client.scenes;


import client.MyFXML;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;


@ExtendWith(ApplicationExtension.class)
public class EditTitleCtrlTest {

    EditTitleCtrl ctrl;

    Scene scene;

    Stage stage;


    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));

        LanguageConf languageConf = new LanguageConf(userConfig);

        var editTitleLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/EditTitle.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new EditTitleCtrl(languageConf),
                StandardCharsets.UTF_8);


        scene = new Scene(editTitleLoader.load());
        ctrl = editTitleLoader.getController();
        stage.setScene(scene);
        stage.show();


    }


    @Test
    public void testInitialize(FxRobot robot) {
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

    }

    @Test
    public void testCancelChangeTitle(FxRobot robot) {
        String newTitle = "new title";
        robot.lookup("#nameTextField").queryAs(TextField.class).setText(newTitle);
        robot.clickOn("#cancelButton");
    }

    @Test
    public void testDisplayEditPage(FxRobot robot) {
        Platform.runLater(() -> {
            Event event = new Event("test");
            ctrl.displayEditEventTitle(new EventPageCtrl(null,null,new TestWebsocket(),null), event, new Stage());
            ctrl.cancelTitle();
        });
        waitForFxEvents();


    }
}
