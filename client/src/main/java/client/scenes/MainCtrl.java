/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

public class MainCtrl {

    private Stage primaryStage;
    private StartScreenCtrl startScreenCtrl;
    private Scene startScreen;
    private Scene adminLogin;

    private LanguageConf languageConf;

    private AdminLoginCtrl adminLoginCtrl;
    private EditParticipantsCtrl editParticipantsCtrl;
    private Scene editParticipants;

    private AddExpenseCtrl addExpenseCtrl;
    private Scene addExpense;

    private EventPageCtrl eventPageCtrl;
    private Scene eventPage;
    private UserConfig userConfig;

    private Scene adminOverview;
    private AdminOverviewCtrl adminOverviewCtrl;
    private final Websocket websocket;

    /**
     * @param websocket the websocket instance
     */
    @Inject
    public MainCtrl(Websocket websocket) {
        this.websocket = websocket;

    }

    /**
     * Initializes the UI
     *
     * @param primaryStage         stage
     * @param languageConf         the language config
     * @param userConfig           the user configuration
     * @param startScreen          controller and scene
     * @param eventPage            controller and scene for event page
     * @param adminLogin           admin login controller and scene
     * @param editParticipantsPage controller and scene for editParticipants
     * @param adminOverview        admin overview controller and scene
     * @param addExpensePage controller and scene for addExpense
     */
    public void initialize(
            Stage primaryStage,
            LanguageConf languageConf,
            UserConfig userConfig,
            Pair<StartScreenCtrl, Parent> startScreen,
            Pair<EventPageCtrl, Parent> eventPage,
            Pair<AdminLoginCtrl, Parent> adminLogin,
            Pair<EditParticipantsCtrl, Parent> editParticipantsPage,
            Pair<AdminOverviewCtrl, Parent> adminOverview,
            Pair<AddExpenseCtrl, Parent> addExpensePage
    ) {

        this.primaryStage = primaryStage;
        this.languageConf = languageConf;
        this.userConfig = userConfig;


        this.adminLoginCtrl = adminLogin.getKey();
        this.adminLogin = new Scene(adminLogin.getValue());

        this.startScreenCtrl = startScreen.getKey();
        this.startScreen = new Scene(startScreen.getValue());

        this.eventPageCtrl = eventPage.getKey();
        this.eventPage = new Scene(eventPage.getValue());


        this.editParticipantsCtrl = editParticipantsPage.getKey();
        this.editParticipants = new Scene(editParticipantsPage.getValue());

        this.addExpenseCtrl = addExpensePage.getKey();
        this.addExpense = new Scene(addExpensePage.getValue());

        this.adminOverviewCtrl = adminOverview.getKey();
        this.adminOverview = new Scene(adminOverview.getValue());

        //showOverview();
        showStartScreen();
        primaryStage.show();


    }

    /**
     * Display start screen
     */
    public void showStartScreen() {
        websocket.disconnect();
        primaryStage.setTitle(languageConf.get("StartScreen.title"));
        startScreenCtrl.reset();
        primaryStage.setScene(startScreen);

    }

    /**
     * Display admin login
     */
    public void showAdminLogin() {
        primaryStage.setTitle(languageConf.get("AdminLogin.title"));
        primaryStage.setScene(adminLogin);
    }

    /**
     * shows the event page
     *
     * @param eventToShow the event to display
     */
    public void showEventPage(Event eventToShow) {
        userConfig.setMostRecentEventCode(eventToShow.getId());
        websocket.connect(eventToShow.getId());
        eventPageCtrl.displayEvent(eventToShow);
        startScreen.setCursor(Cursor.DEFAULT);
        primaryStage.setScene(eventPage);
    }

    /**
     * this method is used to switch back to the event
     * page from the participant/expense editors
     * @param event the event to show
     */
    public void goBackToEventPage(Event event) {
        eventPageCtrl.displayEvent(event);
        primaryStage.setScene(eventPage);
    }

    /**
     * shows the participant editor page
     *
     * @param eventToShow the event to show the participant editor for
     */
    public void showEditParticipantsPage(Event eventToShow) {
        editParticipantsCtrl.displayEditParticipantsPage(eventToShow);
        primaryStage.setTitle(languageConf.get("EditP.editParticipants"));
        primaryStage.setScene(editParticipants);
    }

    /**
     * shows the admin overview
     * @param password admin password
     */
    public void showAdminOverview(String password) {
        adminOverviewCtrl.setPassword(password);
        adminOverviewCtrl.initPoller(5000L); // 5 sec time out
        adminOverviewCtrl.loadAllEvents(); // the password needs to be set before this method
        primaryStage.setTitle(languageConf.get("AdminOverview.title"));
        primaryStage.setScene(adminOverview);
    }

    /**
     * Opens the system file chooser to save something
     *
     * @param fileChooser file chooser
     * @return opened file
     */
    public File showSaveFileDialog(FileChooser fileChooser) {
        return fileChooser.showSaveDialog(primaryStage);
    }

    /**
     * Opens the system file chooser to open multiple files
     *
     * @param fileChooser file chooser
     * @return selected files
     */
    public List<File> showOpenMultipleFileDialog(FileChooser fileChooser) {
        return fileChooser.showOpenMultipleDialog(primaryStage);
    }

    /**
     * shows the add/edit expense page
     * @param eventToShow the event to show the participant editor for
     */
    public void showAddExpensePage(Event eventToShow) {
        addExpenseCtrl.displayAddExpensePage(eventToShow, null);
        addExpenseCtrl.setButton(languageConf.get("AddExp.add"));
        primaryStage.setTitle(languageConf.get("AddExp.addexp"));
        primaryStage.setScene(addExpense);
    }

    /**
     * Handle editing an expense.
     * @param exp The expense to edit.
     * @param ev The event associated with the expense.
     */
    public void handleEditExpense(Expense exp, Event ev) {

        addExpenseCtrl.displayAddExpensePage(ev, exp);
        primaryStage.setTitle(languageConf.get("AddExp.editexp"));
        primaryStage.setScene(addExpense);

        addExpenseCtrl.setButton(languageConf.get("AddExp.save"));
        addExpenseCtrl.setExpenseAuthor(exp.getExpenseAuthor().getName());
        addExpenseCtrl.setPurpose(exp.getPurpose());
        addExpenseCtrl.setAmount(Double.toString(exp.getAmount()));
        addExpenseCtrl.setCurrency(exp.getCurrency());
        addExpenseCtrl.setDate(exp.getDate().toInstant().
                atZone(ZoneId.systemDefault()).toLocalDate());
        addExpenseCtrl.setType(exp.getType());
        addExpenseCtrl.setSplitCheckboxes(exp, ev);

    }
}