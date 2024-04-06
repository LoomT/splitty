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

import client.MockClass.*;
import client.components.ErrorPopupCtrl;
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

public class MainCtrl {

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
    private EditParticipantInterface editParticipantsCtrl;
    private Scene editParticipants;
    private AddExpenseCtrl addExpenseCtrl;
    private Scene addExpense;

    private OpenDebtsPageCtrl openDebtsPageCtrl;
    private Scene openDebtsPage;

    private EventPageCtrl eventPageCtrl;
    private Scene eventPage;

    private EditEventTitleInterface editTitleCtrl;
    private Scene titleChanger;
    private ErrorPopupCtrl errorPopupCtrl;
    private Scene errorPopup;



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

        this.openDebtsPageCtrl = pairCollector.openDebtsPage().getKey();
        this.openDebtsPage = new Scene(pairCollector.openDebtsPage().getValue());

        this.editParticipantsCtrl = pairCollector.editParticipantsPage().getKey();
        this.editParticipants = new Scene(pairCollector.editParticipantsPage().getValue());

        this.addExpenseCtrl = pairCollector.addExpensePage().getKey();
        this.addExpense = new Scene(pairCollector.addExpensePage().getValue());

        this.adminOverviewCtrl = pairCollector.adminOverview().getKey();
        this.adminOverview = new Scene(pairCollector.adminOverview().getValue());

        this.editTitleCtrl = pairCollector.editTitlePage().getKey();
        this.titleChanger = new Scene(pairCollector.editTitlePage().getValue());

        this.errorPopupCtrl = pairCollector.errorPopup().getKey();
        this.errorPopup = new Scene(pairCollector.errorPopup().getValue());

        //showOverview();
        showStartScreen();
        primaryStage.show();


    }

    /**
     * Display start screen
     */
    public void showStartScreen() {
        primaryStage.setTitle(languageConf.get("StartScreen.title"));
        startScreenCtrl.reset();
        primaryStage.setScene(startScreen);
    }

    /**
     * Shows the change
     * @param event current event
     */
    public void showEditTitle(Event event){
        Stage stage = new Stage();
        stage.setScene(titleChanger);
        editTitleCtrl.displayEditEventTitle(eventPageCtrl, event, stage);
    }

    /**
     * Changes the title in the editEventTitle
     * @param title title of the event to be changed to
     */
    public void updateEditTitle(String title){
        editTitleCtrl.changeTitle(title);
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
     * edits the EditParticipantPage without opening it.
     *
     * @param eventToShow the event to update.
     */
    public void updateEditParticipantsPage(Event eventToShow) {
        editParticipantsCtrl.displayEditParticipantsPage(eventToShow);
    }

    /**
     * shows the admin overview
     * @param password admin password
     * @param timeOut time out time in ms
     */
    public void showAdminOverview(String password, long timeOut) {
        adminOverviewCtrl.setPassword(password);
        adminOverviewCtrl.initPoller(timeOut); // 5 sec time out
        adminOverviewCtrl.loadAllEvents(); // the password needs to be set before this method
        primaryStage.setTitle(languageConf.get("AdminOverview.title"));
        primaryStage.setScene(adminOverview);
    }

    /**
     * Show error popup for general usage
     * @param stringToken String token to be used as a variable in the error text
     * @param intToken int token to be used as a variable in the error text
     * @param code Error code of the error as found in ErrorCode enum in ErrorPopupCtrl
     * Check ErrorPopupCtrl for more detailed documentation
     */
    public void showErrorPopup(String code, String stringToken, int intToken){
        errorPopupCtrl.generatePopup(code, stringToken, intToken);
        Stage stage = new Stage();
        stage.setScene(errorPopup);
        stage.setResizable(false);
        stage.setTitle("Error");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
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

    /**
     * Set editTitleCtrl for testing purposes
     * @param editTitleCtrl new EditTitleCtrl
     */
    public void setEditTitleCtrl(EditEventTitleInterface editTitleCtrl) {
        this.editTitleCtrl = editTitleCtrl;
    }

    /**
     * Set editParticipantCtrl for testing purposes
     * @param editParticipantCtrl new editParticipantCtrl
     */
    public void setEditParticipantsCtrl(EditParticipantMock editParticipantCtrl){
        this.editParticipantsCtrl = editParticipantCtrl;
    }

    /**
     * Shows the open debts page
     * @param eventToShow the event to show the open debts for
     */
    public void showDebtsPage(Event eventToShow) {
        openDebtsPageCtrl.displayOpenDebtsPage(eventToShow);
        primaryStage.setScene(openDebtsPage);
    }
}