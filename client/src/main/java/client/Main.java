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
package client;

import client.MockClass.MainCtrlInterface;
import client.scenes.*;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    private static final UserConfig userConfig = INJECTOR.getInstance(UserConfig.class);
    private static final LanguageConf languageConf = INJECTOR.getInstance(LanguageConf.class);

    /**
     * Main class
     *
     * @param args Runtime arguments
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Starts the application
     *
     * @param primaryStage stage
     */
    @Override
    public void start(Stage primaryStage) {
        languageConf.onLanguageChange(() -> {
            // When the language is changed, this function is run
            loadLanguageResourcesAndStart(primaryStage);
        });
        userConfig.onContrastChange(() -> {
            // When the language is changed, this function is run
            loadLanguageResourcesAndStart(primaryStage);
        });
        loadLanguageResourcesAndStart(primaryStage);
    }

    /**
     * Loads/reloads the page with the resources.
     * IMPORTANT: put all the FXML loading in this function, as when the language is changed,
     * this is the function that is rerun to reload the different language bundle.
     *
     * @param primaryStage the primary stage
     */
    public void loadLanguageResourcesAndStart(Stage primaryStage) {  // Load all the FXML here:
        boolean isHighContrast = userConfig.getHighContrast();
        var start = FXML.load(StartScreenCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "StartScreen.fxml"
        );
        var adminLogin = FXML.load(AdminLoginCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "AdminLogin.fxml");
        var eventPage = FXML.load(EventPageCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "EventPage.fxml");
        var editParticipants = FXML.load(EditParticipantsCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "EditParticipants.fxml");
        var adminOverview = FXML.load(AdminOverviewCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "AdminOverview.fxml");
        var addExpense = FXML.load(AddExpenseCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "AddExpense.fxml");
        var titleChanger = FXML.load(EditTitleCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "EditTitle.fxml");
        var addTag = FXML.load(AddTagCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "AddTag.fxml");
        var options = FXML.load(OptionsCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "Options.fxml");
        var statistics = FXML.load(StatisticsCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "Statistics.fxml");
        var addCustomTransaction = FXML.load(AddCustomTransactionCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "AddCustomTransaction.fxml");
        var openDebtsPage = FXML.load(OpenDebtsPageCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "OpenDebtsPage.fxml");
        var inviteMailPage = FXML.load(InviteMailCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "InviteMail.fxml");
        var tagPage = FXML.load(TagPageCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "scenes", "TagPage.fxml");
        var mainCtrl = INJECTOR.getInstance(MainCtrlInterface.class);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("flags/logo.png"));

        mainCtrl.initialize(primaryStage, new PairCollector(start,
                eventPage, adminLogin, editParticipants,
                adminOverview, addExpense, titleChanger, addTag, statistics, options,
                addCustomTransaction, openDebtsPage, inviteMailPage, tagPage)
        );
    }
}