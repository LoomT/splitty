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

import client.components.ErrorPopupCtrl;
import client.scenes.*;
import client.utils.LanguageConf;
import client.utils.UserConfig;
import com.google.inject.Injector;
import javafx.application.Application;
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
    public static void main(String[] args){
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
        var start = FXML.load(
                StartScreenCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "StartScreen.fxml"
        );
        var adminLogin = FXML.load(
                AdminLoginCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "AdminLogin.fxml"
        );
        var eventPage = FXML.load(
                EventPageCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "EventPage.fxml"
        );
        eventPage.getKey().initialize();

        var editParticipants = FXML.load(
                EditParticipantsCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "EditParticipants.fxml"
        );
        var adminOverview = FXML.load(
                AdminOverviewCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "AdminOverview.fxml"
        );
        var errorPopup = FXML.load(
                ErrorPopupCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "ErrorPopup.fxml"
        );
        var addExpense = FXML.load(
                AddExpenseCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "AddExpense.fxml"
        );

        var titleChanger = FXML.load(
                EditTitleCtrl.class,
                languageConf.getLanguageResources(),
                "client", "scenes", "EditTitle.fxml"
        );
        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, new PairCollector(start,
                eventPage, adminLogin, editParticipants,
                adminOverview, addExpense, errorPopup, titleChanger)
        );
    }
}