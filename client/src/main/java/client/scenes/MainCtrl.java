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
import commons.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl {

    private Stage primaryStage;
    private StartScreenCtrl startScreenCtrl;
    private Scene startScreen;
    private Scene adminLogin;

    private LanguageConf languageConf;

    private AdminLoginCtrl adminLoginCtrl;

    private EventPageCtrl eventPageCtrl;
    private Scene eventPage;
    private UserConfig userConfig;

    /**
     * Initializes the UI
     *
     * @param primaryStage stage
     * @param languageConf the language config
     * @param userConfig the user configuration
     * @param startScreen controller and scene
     * @param eventPage controller and scene for eventpage
     *
     * @param adminLogin admin login controller and scene
     */
    public void initialize(
            Stage primaryStage,
            LanguageConf languageConf,
            UserConfig userConfig,
            Pair<StartScreenCtrl, Parent> startScreen,
            Pair<EventPageCtrl, Parent> eventPage,
            Pair<AdminLoginCtrl, Parent> adminLogin
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
     * Display admin login
     */
    public void showAdminLogin() {
        primaryStage.setTitle("Admin Login");
        primaryStage.setScene(adminLogin);
    }

    /**
     * shows the event page
     * @param eventToShow the event to display
     */
    public void showEventPage(Event eventToShow) {
        userConfig.setMostRecentEventCode(eventToShow.getId());
        eventPageCtrl.displayEvent(eventToShow);
        primaryStage.setScene(eventPage);
    }


    /**
     * Getter for startScreenCtrl
     *
     * @return startScreenCtrl
     */
    public StartScreenCtrl getStartScreenCtrl() {
        return startScreenCtrl;
    }


    /**
     * setter for startScreenCtrl
     *
     * @param startScreenCtrl start screen controller
     */
    public void setStartScreenCtrl(StartScreenCtrl startScreenCtrl) {
        this.startScreenCtrl = startScreenCtrl;
    }

    /**
     * Display overview
     */


    /**
     * AdminLoginCtrl getter
     *
     * @return admin login controller
     */
    public AdminLoginCtrl getAdminLoginCtrl() {
        return adminLoginCtrl;
    }

    /**
     * setter for adminLoginCtrl
     *
     * @param adminLoginCtrl admin login controller
     */
    public void setAdminLoginCtrl(AdminLoginCtrl adminLoginCtrl) {
        this.adminLoginCtrl = adminLoginCtrl;
    }


//    public void showOverview() {
//        primaryStage.setTitle("Quotes: Overview");
//        primaryStage.setScene(overview);
//        overviewCtrl.refresh();
//    }
//
//    /**
//     * display adding quote scene
//     */
//    public void showAdd() {
//        primaryStage.setTitle("Quotes: Adding Quote");
//        primaryStage.setScene(add);
//        add.setOnKeyPressed(e -> addCtrl.keyPressed(e));
//    }
}