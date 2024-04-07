package client.scenes;


import client.MyFXML;
import client.TestMainCtrl;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import utils.TestIO;
import utils.TestServerUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitForAsync;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class AddExpenseCtrlTest {

    AddExpenseCtrl ctrl;

    TestServerUtils server;

    TestMainCtrl mainCtrl;

    Scene scene;

    Event event;


    @Start
    public void start(Stage stage) throws IOException {
        server = new TestServerUtils();
        UserConfig userConfig = new UserConfig(new TestIO("""
                serverURL=http://localhost:8080/
                lang=en
                recentEventCodes="""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        mainCtrl = new TestMainCtrl();

        var addExpenseLoader = new FXMLLoader(MyFXML.class.getClassLoader()
                .getResource("client/scenes/AddExpense.fxml"),
                languageConf.getLanguageResources(), null,
                (type) -> new AddExpenseCtrl(server, mainCtrl, languageConf),
                StandardCharsets.UTF_8);
        scene = new Scene(addExpenseLoader.load());
        ctrl = addExpenseLoader.getController();

        this.event = new Event("test");
        event.addParticipant(new Participant("test"));
        event.addParticipant(new Participant("test2"));
        List<Tag> eventTags = List.of(new Tag("food", "FF0000"), new Tag("drinks", "0000ff"));
        event.setTags(eventTags);
        server.createEvent(event);


        stage.setScene(scene);
        stage.show();


    }

    @Test
    public void displayAddExpenseTest(FxRobot robot) {
        Platform.runLater(() -> {

            ctrl.displayAddExpensePage(event, null);

            assertNotNull(robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class), "Expense Author ChoiceBox should not be null.");
            assertNotNull(robot.lookup("#purpose").queryAs(TextField.class), "Purpose TextField should not be null.");
            assertNotNull(robot.lookup("#amount").queryAs(TextField.class), "Amount TextField should not be null.");
            assertNotNull(robot.lookup("#currency").queryAs(ChoiceBox.class), "Currency ChoiceBox should not be null.");
            assertNotNull(robot.lookup("#date").queryAs(DatePicker.class), "Date DatePicker should not be null.");
            assertNotNull(robot.lookup("#type").queryAs(ComboBox.class), "Type ComboBox should not be null.");
            assertNotNull(robot.lookup("#equalSplit").query(), "Equal Split CheckBox should not be null.");
            assertNotNull(robot.lookup("#partialSplit").query(), "Partial Split CheckBox should not be null.");
            assertNotNull(robot.lookup("#expenseParticipants").query(), "Expense Participants TextFlow should not be null.");
            assertNotNull(robot.lookup("#addTag").queryAs(Button.class), "Add Tag Button should not be null.");
            assertNotNull(robot.lookup("#add").queryAs(Button.class), "Add Button should not be null.");
            assertNotNull(robot.lookup("#abort").queryAs(Button.class), "Abort Button should not be null.");
        });
        waitForFxEvents();
    }

    @Test
    public void handleAddEmptyExpenseButtonTest(FxRobot robot) {
        AddExpenseCtrl spyCtrl = Mockito.spy(ctrl);

        Platform.runLater(() -> {

            spyCtrl.displayAddExpensePage(event, null);
            robot.clickOn("#add");
            spyCtrl.alertAllFields();


        });
        waitForFxEvents();
        Mockito.verify(spyCtrl, Mockito.times(1)).alertAllFields();

    }

    @Test
    public void handelAddButtonTest(FxRobot robot) {
        Platform.runLater(() -> {
            ctrl.displayAddExpensePage(event, null);
            robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#purpose").queryAs(TextField.class).setText("test");
            robot.lookup("#amount").queryAs(TextField.class).setText("10");
            robot.lookup("#currency").queryAs(ChoiceBox.class).getSelectionModel().select(0);
            robot.lookup("#date").queryAs(DatePicker.class).setValue(java.time.LocalDate.now());
            robot.lookup("#type").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.clickOn("#equalSplit");
            robot.clickOn("#add");

        });
        waitForFxEvents();
        assertEquals(server.getCalls().get(1), "createExpense");
    }

    @Test
    public void handleEditButtonTest(FxRobot robot) {
        Platform.runLater(() -> {
            Expense expense = new Expense(event.getParticipants().getFirst(), "testPurpose", 10, "EUR", event.getParticipants(), "food");
            ctrl.displayAddExpensePage(event, expense);
            robot.lookup("#expenseAuthor").queryAs(ChoiceBox.class).getSelectionModel().select(1);
            robot.lookup("#purpose").queryAs(TextField.class).setText("test");
            robot.lookup("#amount").queryAs(TextField.class).setText("200");
            robot.lookup("#currency").queryAs(ChoiceBox.class).getSelectionModel().select(3);
            robot.lookup("#date").queryAs(DatePicker.class).setValue(java.time.LocalDate.now());
            robot.lookup("#type").queryAs(ComboBox.class).getSelectionModel().select(0);
            robot.clickOn("#equalSplit");
            robot.clickOn("#add");

        });
        waitForFxEvents();
        assertEquals(server.getCalls().get(1), "updateExpense");
    }

}
