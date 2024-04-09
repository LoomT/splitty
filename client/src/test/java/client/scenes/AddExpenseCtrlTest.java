package client.scenes;


import client.MyFXML;
import client.TestMainCtrl;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.currency.CurrencyConverter;
import commons.*;
import commons.Tag;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import utils.FileManagerMock;
import utils.TestIO;
import utils.TestServerUtils;
import utils.TestWebsocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(ApplicationExtension.class)
public class AddExpenseCtrlTest {

    AddExpenseCtrl ctrl;

    TestServerUtils server;

    TestMainCtrl mainCtrl;

    Scene scene;

    TestWebsocket websocket;

    Event event;

    Stage stage;


    @Start
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        websocket = new TestWebsocket();
        server = new TestServerUtils(websocket);
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        mainCtrl = new TestMainCtrl();
        CurrencyConverter currencyConverter = new CurrencyConverter(server, new FileManagerMock(), languageConf);

        var addExpenseLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/AddExpense.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AddExpenseCtrl(mainCtrl,server, websocket, languageConf, currencyConverter, userConfig),
                StandardCharsets.UTF_8);
        scene = new Scene(addExpenseLoader.load());
        ctrl = addExpenseLoader.getController();

        this.event = new Event("test");
        event = server.createEvent(event);
        Participant p1 = new Participant("test");
        Participant p2 = new Participant("test2");
        server.createParticipant(event.getId(), p1);
        server.createParticipant(event.getId(), p2);
        Tag t1 = new Tag("food", "FF0000");
        Tag t2 = new Tag("drinks", "0000ff");
        server.addTag(event.getId(), t1);
        server.addTag(event.getId(), t2);
        event = server.getEvent(event.getId());
        event.setTags(List.of(t1, t2));



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
    @Order(1)
    public void displayAddExpenseTest(FxRobot robot) {
        Platform.runLater(() -> {

            ctrl.displayAddExpensePage(event, null);
            assertFalse(robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getItems().isEmpty());
            assertTrue(robot.lookup("#purpose").queryAs(TextField.class).getText().isEmpty());
            assertTrue(robot.lookup("#amount").queryAs(TextField.class).getText().isEmpty());
            assertFalse(robot.lookup("#currency").queryAs(ComboBox.class).getItems().isEmpty());
            assertFalse(robot.lookup("#date").queryAs(DatePicker.class).getValue().toString().isEmpty());
            assertFalse(robot.lookup("#type").queryAs(ComboBox.class).getItems().isEmpty());
            assertFalse(robot.lookup("#expenseParticipants").queryAs(TextFlow.class).getChildren().isEmpty());
            assertFalse(robot.lookup("#partialSplit").queryAs(CheckBox.class).isSelected());
            assertFalse(robot.lookup("#equalSplit").queryAs(CheckBox.class).isSelected());
        });
        waitForFxEvents();
    }

    @Test
    @Order(2)
    public void handleAddEmptyExpenseButtonTest(FxRobot robot) {
        AddExpenseCtrl spyCtrl = Mockito.spy(ctrl);

        Platform.runLater(() -> {
            spyCtrl.displayAddExpensePage(event, null);
            robot.clickOn("#add");
        });

        waitForFxEvents();
        Mockito.verify(spyCtrl, Mockito.times(1)).alertAllFields();

    }

    @Test
    @Order(3)
    public void handleAddButtonTest(FxRobot robot) {
        Platform.runLater(() -> {

            server.getCalls().clear();
            server.getStatuses().clear();
            websocket.resetTriggers();

            ctrl.displayAddExpensePage(event, null);

            robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#purpose").queryAs(TextField.class).setText("test");
            robot.lookup("#amount").queryAs(TextField.class).setText("10");
            robot.lookup("#currency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#date").queryAs(DatePicker.class).setValue(java.time.LocalDate.now());
            robot.lookup("#type").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#equalSplit").queryAs(CheckBox.class).setSelected(true);
            robot.clickOn("#add");

        });
        waitForFxEvents();
        assertEquals(server.getCalls().getFirst(), "createExpense");
        assertEquals(server.getStatuses().getFirst(), 204);
        assertTrue(websocket.hasActionBeenTriggered(WebsocketActions.ADD_EXPENSE));
        assertNotNull(websocket.getPayloads().getFirst());
        assertTrue(websocket.getPayloads().getFirst().getClass().equals(Expense.class));


    }

    @Test
    @Order(4)
    public void handleEditButtonTest(FxRobot robot) {
        Platform.runLater(() -> {
            Tag tag = new Tag("food", "FF0000");
            Expense expense = new Expense(event.getParticipants().getFirst(), "testPurpose", 10, "EUR", event.getParticipants(), tag);
            server.createExpense(event.getId(), expense);
            int expenseID = (int) server.getEvent(event.getId()).getExpenses().getFirst().getId();
            expense.setId(expenseID);
            expense.setEventID(event.getId());
            server.getCalls().clear();
            server.getStatuses().clear();
            websocket.resetTriggers();
            ctrl.displayAddExpensePage(event, expense);
            robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#purpose").queryAs(TextField.class).setText("test");
            robot.lookup("#amount").queryAs(TextField.class).setText("200");
            robot.lookup("#currency").queryAs(ComboBox.class).getSelectionModel().select(3);
            robot.lookup("#date").queryAs(DatePicker.class).setValue(java.time.LocalDate.now());
            robot.lookup("#type").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.clickOn("#equalSplit");
            robot.clickOn("#add");

        });
        waitForFxEvents();
        assertEquals(server.getCalls().getFirst(), "updateExpense");
        assertEquals(server.getStatuses().getFirst(), 204);
        assertTrue(websocket.hasActionBeenTriggered(WebsocketActions.UPDATE_EXPENSE));
        assertNotNull(websocket.getPayloads().getFirst());
        assertEquals(websocket.getPayloads().getFirst().getClass(), Expense.class);

    }

    @Test
    @Order(5)
    public void handleAddTagTest(FxRobot robot) {
        Platform.runLater(() -> {

            ctrl.displayAddExpensePage(event, null);
            robot.clickOn("#addTag");

        });
        waitForFxEvents();
        assertEquals(mainCtrl.getScenes().getFirst(), "AddTagPage");

    }

    @Test
    @Order(6)
    public void testGetSelectedParticipants(FxRobot robot) {
        Platform.runLater(() -> {
            ctrl.displayAddExpensePage(event, null);
            robot.clickOn("#partialSplit");
            TextFlow textFlow = robot.lookup("#expenseParticipants").queryAs(TextFlow.class);
            CheckBox first = (CheckBox) textFlow.getChildren().getFirst();
            assertEquals(first.getText(), "test");

        });
    }

    @Test
    @Order(7)
    public void testHandlePartialSplit(FxRobot robot) {
        Platform.runLater(() -> {
            server.getCalls().clear();
            server.getStatuses().clear();
            websocket.resetTriggers();
            ctrl.displayAddExpensePage(event, null);
            robot.clickOn("#partialSplit");
            TextFlow textFlow = robot.lookup("#expenseParticipants").queryAs(TextFlow.class);
            CheckBox first = (CheckBox) textFlow.getChildren().getFirst();
            first.setSelected(true);
            robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#purpose").queryAs(TextField.class).setText("test");
            robot.lookup("#amount").queryAs(TextField.class).setText("10");
            robot.lookup("#currency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#date").queryAs(DatePicker.class).setValue(java.time.LocalDate.now());
            robot.lookup("#type").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.clickOn("#add");
        });
        waitForFxEvents();
        assertEquals(server.getCalls().getFirst(), "createExpense");
        assertEquals(server.getStatuses().getFirst(), 204);
        assertTrue(websocket.hasActionBeenTriggered(WebsocketActions.ADD_EXPENSE));
        assertNotNull(websocket.getPayloads().getFirst());
        assertEquals(websocket.getPayloads().getFirst().getClass(), Expense.class);
    }

    @Test
    @Order(8)
    public void testCatchNumberFormatException (FxRobot robot) {
        Platform.runLater(() -> {
            server.getCalls().clear();
            ctrl.displayAddExpensePage(event, null);
            robot.lookup("#amount").queryAs(TextField.class).setText("abc");
            robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#purpose").queryAs(TextField.class).setText("test");
            robot.lookup("#currency").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.lookup("#date").queryAs(DatePicker.class).setValue(java.time.LocalDate.now());
            robot.lookup("#type").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.clickOn("#equalSplit");
            robot.clickOn("#add");
        });
        waitForFxEvents();
        assertTrue(server.getCalls().isEmpty());
    }


}
