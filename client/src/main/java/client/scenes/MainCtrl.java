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
import client.components.FlagListCell;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import client.utils.Websocket;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import javafx.event.EventTarget;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

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

    private OpenDebtsPageCtrl openDebtsPageCtrl;
    private Scene openDebtsPage;

    private EventPageCtrl eventPageCtrl;
    private Scene eventPage;

    private EditTitleCtrl editTitleCtrl;
    private Scene titleChanger;

    private AddTagCtrl addTagCtrl;
    private Scene addTag;

    private OptionsCtrl optionsCtrl;
    private Scene options;
    private StatisticsCtrl statisticsCtrl;
    private Scene statistics;


    private AddCustomTransactionCtrl addCustomTransactionCtrl;
    private Scene addCustomTransaction;

    private boolean startPage = true;
    private Event event;

    private TagPageCtrl tagPageCtrl;
    private Scene tagPage;

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
        this.userConfig = userConfig;}

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

        this.addTagCtrl = pairCollector.addTagPage().getKey();
        this.addTag = new Scene(pairCollector.addTagPage().getValue());

        this.optionsCtrl = pairCollector.options().getKey();
        this.options = new Scene(pairCollector.options().getValue());

        this.addCustomTransactionCtrl = pairCollector.addCustomTransaction().getKey();
        this.addCustomTransaction = new Scene(pairCollector.addCustomTransaction().getValue());

        this.statisticsCtrl = pairCollector.statisticsPage().getKey();
        this.statistics = new Scene(pairCollector.statisticsPage().getValue());

        this.tagPageCtrl = pairCollector.tagPage().getKey();
        this.tagPage = new Scene(pairCollector.tagPage().getValue());

        initializeShortcuts();
        //showOverview();
        showStartScreen();
        if(startPage){
            showStartScreen();
        } else {
            showEventPage(event);
        }
        primaryStage.show();

    }

    /**
     * Initializes the shortcuts for all scenes
     */
    public void initializeShortcuts(){

        startScreenCtrl.initializeShortcuts(startScreen);
        eventPageCtrl.initializeShortcuts(eventPage);
        editParticipantsCtrl.initializeShortcuts(editParticipants);
        addExpenseCtrl.initializeShortcuts(addExpense);
        adminLoginCtrl.initializeShortcuts(adminLogin);
        adminOverviewCtrl.initializeShortcuts(adminOverview);
        editTitleCtrl.initializeShortcuts(titleChanger);
        optionsCtrl.initializeShortcuts(options);
        openDebtsPageCtrl.initializeShortcuts(openDebtsPage);
        addCustomTransactionCtrl.initializeShortcuts(addCustomTransaction);
    }

    /**
     * Initializes an event listener for a scene and executes the runnable
     * if a key is inputted.
     * @param target target the event listener should be initialised in
     * @param function function to be executed if the criteria is met
     * @param key Keycode to be checked.
     */
    public static void checkKey(EventTarget target, Runnable function, KeyCode key) {
        target.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == key) {
                System.out.println("Key Pressed: " + ke.getCode());
                try {
                    function.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ke.consume(); // <-- stops passing the event to next node
            }
        });
    }

    /**
     * Initializes an event listener for a scene and executes the runnable
     * if a key is inputted in a particular field.
     * @param target target the event listener should be initialised in
     * @param function function to be executed if the criteria is met
     * @param key Keycode to be checked.
     * @param field field that should be in the focus for the function to be executed
     */
    public static void checkKey(EventTarget target, Runnable function, Object field, KeyCode key){
        target.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == key) {
                if(ke.getTarget().equals(field)){
                    System.out.println("Key Pressed: " + ke.getCode());
                    System.out.println(ke.getTarget());
                    function.run();
                    ke.consume(); // <-- stops passing the event to next node
                }
            }
        });
    }

    /**
     * Display start screen
     */
    @Override
    public void showStartScreen() {
        primaryStage.setTitle(languageConf.get("StartScreen.title"));
        startScreenCtrl.reset();
        startScreenCtrl.reloadEventCodes();
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
        stage.getIcons().add(primaryStage.getIcons().getFirst());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(languageConf.get("TitleChanger.pageTitle"));
        stage.setResizable(false);
        stage.initOwner(primaryStage);
        editTitleCtrl.displayEditEventTitle(event, stage);
        stage.show();
    }

    /**
     * Display admin login
     */
    @Override
    public void showAdminLogin() {
        primaryStage.setTitle(languageConf.get("AdminLogin.title"));
        adminLoginCtrl.display();
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
        startPage = false;
        this.event = eventToShow;
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
        primaryStage.setTitle(languageConf.get("EventPage.title"));
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
        stage.getIcons().add(primaryStage.getIcons().getFirst());
        stage.initOwner(primaryStage);
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

        addExpenseCtrl.setAmount(exp.getAmount(), exp.getDate(), exp.getCurrency());
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
        primaryStage.setTitle(languageConf.get("StartScreen.title"));
        startScreenCtrl.reset();
        primaryStage.setScene(startScreen);
        startScreenCtrl.showServerNotFoundError();
    }
    /**
     * display the statistics page
     * @param event event to display
     */
    @Override
    public void showStatisticsPage(Event event) {
        statisticsCtrl.displayStatisticsPage(event);
        primaryStage.setTitle(languageConf.get("Statistics.title"));
        primaryStage.setScene(statistics);
        statistics.setCursor(Cursor.DEFAULT);
    }

    /**
     * Initializes a new stage with options
     * and opens it
     */
    @Override
    public void openOptions() {
        Stage stage = new Stage();
        stage.setScene(options);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(languageConf.get("Options.title"));
        optionsCtrl.display(stage);
        stage.setResizable(false);
        stage.initOwner(primaryStage);
        stage.getIcons().add(primaryStage.getIcons().getFirst());
        stage.show();
    }

    /**
     * Shows the open debts page
     * @param eventToShow the event to show the open debts for
     */
    @Override
    public void showDebtsPage(Event eventToShow) {
        openDebtsPageCtrl.open();
        openDebtsPageCtrl.displayOpenDebtsPage(eventToShow);
        primaryStage.setTitle(languageConf.get("OpenDebts.title"));
        primaryStage.setScene(openDebtsPage);
    }

    /**
     * Display a window for adding a custom transaction
     * @param event event to load
     */
    @Override
    public void showAddCustomTransaction(Event event) {
        Stage stage = new Stage();
        stage.setTitle(languageConf.get("AddCustomTransaction.titlebar"));
        addCustomTransactionCtrl.display(event, stage);
        stage.setScene(addCustomTransaction);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);
        stage.show();
    }

    /**
     * @param languageChoiceBox method to initialize the language switcher
     */
    public void initLangChoiceBox(ComboBox<String> languageChoiceBox){
        languageChoiceBox.setValue(languageConf.getCurrentLocaleString());
        final String downloadTemplateOption = "Download Template";
        if(languageChoiceBox.getItems().isEmpty()) {
            languageChoiceBox.getItems().addAll(languageConf.getAvailableLocalesString());
            languageChoiceBox.getItems().add(downloadTemplateOption);
        }
        languageChoiceBox.setButtonCell(new FlagListCell(languageConf));
        languageChoiceBox.setCellFactory(param -> new FlagListCell(languageConf));
        languageChoiceBox.setOnAction(event -> {
            String selectedOption = languageChoiceBox.getValue();
            if (selectedOption.equals(downloadTemplateOption)) {

                downloadTemplate();
                languageChoiceBox.setValue(languageConf.getCurrentLocaleString());
            } else {

                languageConf.changeCurrentLocaleTo(selectedOption);
            }
        });
    }

    /**
     * @param  page boolean for the startPage (true) or the eventPage (false)
     */
    public void setStartPage(boolean page) {this.startPage = page;}

    /**
     *
     * Downloads the template
     */
    private void downloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("template.properties");

        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Properties files (*.properties)",
                        "*.properties");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = showSaveFileDialog(fileChooser);
        if (file == null) {

            return;
        }
        ResourceBundle bundle = ResourceBundle.getBundle("languages", Locale.of("template"));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<String> keyList = new ArrayList<>();
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                keyList.add(key);
            }
            keyList.sort(String::compareTo);
            for (String key : keyList) {
                writer.write(key + "=" + bundle.getString(key) + "\n");
            }
            System.out.println("Template downloaded successfully to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("An error occurred while writing the template file: "
                    + e.getMessage());
        }

    }



    @Override
    public void showTagPage(Event event, PieChart pc) {
        tagPageCtrl.displayTagPage(event, pc);
        primaryStage.setTitle("Tags overview");
        primaryStage.setScene(tagPage);
    }
}