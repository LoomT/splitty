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

import client.MockClass.MainCtrlInterface;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

public class MainCtrl implements MainCtrlInterface{

    private final UserConfig userConfig;
    private final LanguageConf languageConf;
    private final Websocket websocket;

    private Stage primaryStage;
    private StartScreenCtrl startScreenCtrl;
    private Scene startScreen;
    private AdminLoginCtrl adminLoginCtrl;
    private Scene adminLogin;
    private AdminOverviewCtrl adminOverviewCtrl;
    private Scene adminOverview;
    private EditParticipantsCtrl editParticipantsCtrl;
    private Scene editParticipants;
    private AddExpenseCtrl addExpenseCtrl;
    private Scene addExpense;
    private EventPageCtrl eventPageCtrl;
    private Scene eventPage;

    private EditTitleCtrl editTitleCtrl;
    private Scene titleChanger;
    private AddTagCtrl addTagCtrl;
    private Scene addTag;



    /**
     * @param websocket the websocket instance
     * @param languageConf the language config
     * @param userConfig the user configuration
     */
    @Inject
    public MainCtrl(Websocket websocket, LanguageConf languageConf,
                    UserConfig userConfig) {
        this.websocket = websocket;
        this.languageConf = languageConf;
        this.userConfig = userConfig;
    }

    /**
     * Initializes the UI
     *
     * @param primaryStage         stage
     * @param pairCollector        collector for all of pairs
     */
    @Override
    public void initialize(
            Stage primaryStage,
            PairCollector pairCollector
    ) {

        this.primaryStage = primaryStage;

        this.adminLoginCtrl = pairCollector.adminLogin().getKey();
        this.adminLogin = new Scene(pairCollector.adminLogin().getValue());

        this.startScreenCtrl = pairCollector.startScreen().getKey();
        this.startScreen = new Scene(pairCollector.startScreen().getValue());

        this.eventPageCtrl = pairCollector.eventPage().getKey();
        this.eventPage = new Scene(pairCollector.eventPage().getValue());


        this.editParticipantsCtrl = pairCollector.editParticipantsPage().getKey();
        this.editParticipants = new Scene(pairCollector.editParticipantsPage().getValue());

        this.addExpenseCtrl = pairCollector.addExpensePage().getKey();
        this.addExpense = new Scene(pairCollector.addExpensePage().getValue());

        this.adminOverviewCtrl = pairCollector.adminOverview().getKey();
        this.adminOverview = new Scene(pairCollector.adminOverview().getValue());

        this.editTitleCtrl = pairCollector.editTitlePage().getKey();
        this.titleChanger = new Scene(pairCollector.editTitlePage().getValue());

        this.addTagCtrl = pairCollector.addTagPage().getKey();
        this.addTag = new Scene(pairCollector.addTagPage().getValue());

        //showOverview();
        showStartScreen();
        primaryStage.show();


    }

    /**
     * Display start screen
     */
    @Override
    public void showStartScreen() {
        primaryStage.setTitle(languageConf.get("StartScreen.title"));
        startScreenCtrl.reset();
        primaryStage.setScene(startScreen);
    }

    /**
     * Shows the change
     * @param event current event
     */
    @Override
    public void showEditTitle(Event event){
        Stage stage = new Stage();
        stage.setScene(titleChanger);
        editTitleCtrl.displayEditEventTitle(event, stage);
    }

    /**
     * Display admin login
     */
    @Override
    public void showAdminLogin() {
        primaryStage.setTitle(languageConf.get("AdminLogin.title"));
        primaryStage.setScene(adminLogin);
    }

    /**
     * shows the event page
     *
     * @param eventToShow the event to display
     */
    @Override
    public void showEventPage(Event eventToShow) {
        userConfig.setMostRecentEventCode(eventToShow.getId());
        websocket.connect(eventToShow.getId());
        eventPageCtrl.displayEvent(eventToShow);
        startScreen.setCursor(Cursor.DEFAULT);
        primaryStage.setTitle(languageConf.get("EventPage.title"));
        primaryStage.setScene(eventPage);
    }

    /**
     * this method is used to switch back to the event
     * page from the participant/expense editors
     * @param event the event to show
     */
    @Override
    public void goBackToEventPage(Event event) {
        eventPageCtrl.displayEvent(event);
        primaryStage.setScene(eventPage);
    }

    /**
     * shows the participant editor page
     *
     * @param eventToShow the event to show the participant editor for
     */
    @Override
    public void showEditParticipantsPage(Event eventToShow) {
        editParticipantsCtrl.displayEditParticipantsPage(eventToShow);
        primaryStage.setTitle(languageConf.get("EditP.editParticipants"));
        primaryStage.setScene(editParticipants);
    }

    /**
     * shows the admin overview
     * @param password admin password
     * @param timeOut time out time in ms
     */
    @Override
    public void showAdminOverview(String password, long timeOut) {
        adminOverviewCtrl.setPassword(password);
        adminOverviewCtrl.initPoller(timeOut); // 5 sec time out
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
    @Override
    public File showSaveFileDialog(FileChooser fileChooser) {
        return fileChooser.showSaveDialog(primaryStage);
    }

    /**
     * Opens the system file chooser to open multiple files
     *
     * @param fileChooser file chooser
     * @return selected files
     */
    @Override
    public List<File> showOpenMultipleFileDialog(FileChooser fileChooser) {
        return fileChooser.showOpenMultipleDialog(primaryStage);
    }

    /**
     * shows the add/edit expense page
     * @param eventToShow the event to show the participant editor for
     */
    @Override
    public void showAddExpensePage(Event eventToShow) {
        addExpenseCtrl.displayAddExpensePage(eventToShow, null);
        addExpenseCtrl.setButton(languageConf.get("AddExp.add"));
        primaryStage.setTitle(languageConf.get("AddExp.addexp"));
        primaryStage.setScene(addExpense);
        primaryStage.setResizable(false);
    }

    /**
     * show the add tag page
     * @param event event to show
     */
    @Override
    public void showAddTagPage(Event event) {
        Stage stage = new Stage();
        addTagCtrl.displayAddTagPage(event, stage);
        stage.setTitle(languageConf.get("AddTag.addtag"));
        stage.setScene(addTag);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    /**
     * Handle editing an expense.
     * @param exp The expense to edit.
     * @param ev The event associated with the expense.
     */
    @Override
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

    /**
     * Disconnects from the server and shows an error
     */
    @Override
    public void handleServerNotFound() {
        websocket.disconnect();
        adminOverviewCtrl.stopPoller();
        showStartScreen();
        startScreenCtrl.showServerNotFoundError();
    }
}