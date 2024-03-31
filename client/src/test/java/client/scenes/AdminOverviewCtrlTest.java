package client.scenes;

import client.MyFXML;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import utils.TestIO;
import utils.TestServerUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class AdminOverviewCtrlTest {
    AdminOverviewCtrl ctrl;
    TestServerUtils server;
    @Start
    public void start(Stage stage) throws IOException {
        server = new TestServerUtils();
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        MainCtrl mainCtrl = new MainCtrl(null, languageConf, userConfig);

        var adminOverviewLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/AdminOverview.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AdminOverviewCtrl(server, mainCtrl, userConfig, languageConf),
                StandardCharsets.UTF_8);
        Scene scene = new Scene(adminOverviewLoader.load());
        ctrl = adminOverviewLoader.getController();

        ctrl.setPassword("password");
        ctrl.initPoller(50L); // 0.5 sec time out
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

    // not sure how to test concurrency
//    @Test
//    void initPollerTimeOut() throws InterruptedException {
//        Thread.sleep(200);
//        assertTrue(server.isPolled());
//        assertTrue(server.getConcurrentStatuses().contains(408));
//    }
//
//    @Test
//    void initPollerResponse() throws InterruptedException {
//        Thread.sleep(200);
//        server.createEvent(new Event("title"));
//        Thread.sleep(500);
//        System.out.println(server.getConcurrentStatuses());
//        assertTrue(server.isPolled());
//        assertTrue(server.getConcurrentStatuses().contains(204));
//    }
//
//    @Test
//    void initPollerIncorrectPassword() throws InterruptedException {
//        ctrl.setPassword("forgor");
//        Thread.sleep(600);
//        server.createEvent(new Event("title"));
//        Thread.sleep(600);
//        assertTrue(server.isPolled());
//        assertTrue(server.getConcurrentStatuses().contains(401));
//        assertFalse(server.getConcurrentStatuses().contains(204));
//    }
//
//    @Test
//    void stopPoller() throws InterruptedException {
//        ctrl.stopPoller();
//        Thread.sleep(500);
//        server.createEvent(new Event("title"));
//        Thread.sleep(500);
//        assertTrue(server.isPolled());
//        assertFalse(server.getConcurrentStatuses().contains(204));
//    }
}
