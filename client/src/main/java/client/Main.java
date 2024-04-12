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
import org.jetbrains.annotations.NotNull;

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
        FXMLloading result = getFxmLloading(isHighContrast);
        var mainCtrl = INJECTOR.getInstance(MainCtrlInterface.class);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("client/scenes/application_logo.png"));

        mainCtrl.initialize(primaryStage, new PairCollector(result.start(),
                result.eventPage(), result.adminLogin(), result.editParticipants(),
                result.adminOverview(), result.addExpense(),
                result.titleChanger(), result.addTag(), result.options(),
                result.addCustomTransaction(), result.openDebtsPage())
        );
    }


    /**
     * loads the FXML files
     *
     * @param isHighContrast high contrast mode
     * @return FXMLoading object
     */
    @NotNull
    private static FXMLloading getFxmLloading(boolean isHighContrast) {
        var start = FXML.load(StartScreenCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "StartScreen.fxml"
        );
        var adminLogin = FXML.load(AdminLoginCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "AdminLogin.fxml"
        );
        var eventPage = FXML.load(EventPageCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "EventPage.fxml"
        );
        var editParticipants = FXML.load(EditParticipantsCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "EditParticipants.fxml"
        );
        var adminOverview = FXML.load(AdminOverviewCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "AdminOverview.fxml"
        );
        var addExpense = FXML.load(AddExpenseCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "AddExpense.fxml"
        );
        var titleChanger = FXML.load(EditTitleCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "EditTitle.fxml"
        );
        var addTag = FXML.load(AddTagCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "AddTag.fxml"
        );
        var options = FXML.load(OptionsCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "Options.fxml"
        );
        var addCustomTransaction = FXML.load(AddCustomTransactionCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "AddCustomTransaction.fxml"
        );
        var openDebtsPage = FXML.load(OpenDebtsPageCtrl.class,
                languageConf.getLanguageResources(), isHighContrast,
                "client", "scenes", "OpenDebtsPage.fxml"
        );
        return new FXMLloading(start, adminLogin, eventPage,
                editParticipants, adminOverview, addExpense, titleChanger,
                addTag, options, addCustomTransaction, openDebtsPage);
    }

    private record FXMLloading(javafx.util.Pair<StartScreenCtrl,
            javafx.scene.Parent> start, javafx.util.Pair<AdminLoginCtrl,
            javafx.scene.Parent> adminLogin, javafx.util.Pair<EventPageCtrl,
            javafx.scene.Parent> eventPage, javafx.util.Pair<EditParticipantsCtrl,
            javafx.scene.Parent> editParticipants, javafx.util.Pair<AdminOverviewCtrl,
            javafx.scene.Parent> adminOverview, javafx.util.Pair<AddExpenseCtrl,
            javafx.scene.Parent> addExpense, javafx.util.Pair<EditTitleCtrl,
            javafx.scene.Parent> titleChanger, javafx.util.Pair<AddTagCtrl,
            javafx.scene.Parent> addTag, javafx.util.Pair<OptionsCtrl,
            javafx.scene.Parent> options, javafx.util.Pair<AddCustomTransactionCtrl,
            javafx.scene.Parent> addCustomTransaction, javafx.util.Pair<OpenDebtsPageCtrl,
            javafx.scene.Parent> openDebtsPage) {
    }
}