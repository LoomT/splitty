package client.scenes;

import client.MyFXML;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import utils.TestIO;
import utils.TestServerUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var startScreenLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/StartScreen.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new StartScreenCtrl(server, mainCtrl, languageConf, userConfig, null),
                StandardCharsets.UTF_8);
        Parent startScreenParent = startScreenLoader.load();
        var adminLoginLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/AdminLogin.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AdminLoginCtrl(server, mainCtrl),
                StandardCharsets.UTF_8);
        Parent adminLoginParent = adminLoginLoader.load();
        var adminOverviewLoader = new FXMLLoader(MyFXML.class.getClassLoader().getResource("client/scenes/AdminOverview.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AdminOverviewCtrl(server, mainCtrl, userConfig, languageConf),
                StandardCharsets.UTF_8);
        Parent adminOverviewParent = adminOverviewLoader.load();
        ctrl = adminOverviewLoader.getController();
        mainCtrl.initialize(stage,
                new Pair<>(startScreenLoader.getController(), startScreenParent),
                new Pair<>(null, new StackPane()),
                new Pair<>(adminLoginLoader.getController(), adminLoginParent),
                new Pair<>(null, new StackPane()),
                new Pair<>(ctrl, adminOverviewParent),
                new Pair<>(null, new StackPane()));
        mainCtrl.showAdminOverview("password", 500L);
    }

    @BeforeAll
    static void setUp() {
//        System.setProperty("testfx.robot", "glass");
//        System.setProperty("testfx.headless", "true");
//        System.setProperty("prism.order", "sw");
//        System.setProperty("prism.text", "t2k");
//        System.setProperty("java.awt.headless", "true");
    }
    @Test
    void initPollerTimeOut() throws InterruptedException {
        Thread.sleep(600);
        assertTrue(server.isPolled());
        assertTrue(server.getStatuses().contains(408));
    }

    @Test
    void initPollerResponse() throws InterruptedException {
        Thread.sleep(200);
        server.createEvent(new Event("title"));
        Thread.sleep(200);
        assertTrue(server.isPolled());
        assertTrue(server.getStatuses().contains(204));
    }

    @Test
    void initPollerIncorrectPassword() throws InterruptedException {
        ctrl.setPassword("forgor");
        Thread.sleep(600);
        server.createEvent(new Event("title"));
        Thread.sleep(600);
        assertTrue(server.isPolled());
        assertTrue(server.getStatuses().contains(401));
        assertFalse(server.getStatuses().contains(204));
    }

    @Test
    void stopPoller() throws InterruptedException {
        ctrl.stopPoller();
        Thread.sleep(700);
        server.createEvent(new Event("title"));
        Thread.sleep(500);
        assertTrue(server.isPolled());
        assertFalse(server.getStatuses().contains(204));
    }
}
