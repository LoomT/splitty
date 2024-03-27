package client.scenes;

import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestIO;
import utils.TestServerUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminOverviewCtrlTest {
    AdminOverviewCtrl ctrl;
    TestServerUtils server = new TestServerUtils();
    @BeforeEach
    void setUp() throws IOException {
        MainCtrl mainCtrl = new MainCtrl(null);
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes=hello,there"""));
        ctrl = new AdminOverviewCtrl(server, mainCtrl, userConfig, new LanguageConf(userConfig));
    }
    @Test
    void initPollerTimeOut() throws InterruptedException {
        ctrl.setPassword("password");
        ctrl.initPoller(500L);
        Thread.sleep(600);
        assertTrue(server.getCalls().contains("pollEvents"));
        assertTrue(server.getStatuses().contains(408));
    }

    @Test
    void initPollerResponse() throws InterruptedException {
        ctrl.setPassword("password");
        ctrl.initPoller(500L);
        Thread.sleep(100);
        server.createEvent(new Event("title"));
        Thread.sleep(500);
        assertTrue(server.getCalls().contains("pollEvents"));
        assertTrue(server.getStatuses().contains(204));
    }

    @Test
    void initPollerIncorrectPassword() throws InterruptedException {
        ctrl.setPassword("forgor");
        ctrl.initPoller(500L);
        server.createEvent(new Event("title"));
        Thread.sleep(600);
        assertTrue(server.getCalls().contains("pollEvents"));
        assertTrue(server.getStatuses().contains(401));
        assertFalse(server.getStatuses().contains(204));
    }

    @Test
    void stopPoller() throws InterruptedException {
        ctrl.setPassword("password");
        ctrl.initPoller(500L);
        Thread.sleep(600);
        ctrl.stopPoller();
        server.createEvent(new Event("title"));
        Thread.sleep(600);
        assertTrue(server.getCalls().contains("pollEvents"));
        assertFalse(server.getStatuses().contains(204));
    }
}
