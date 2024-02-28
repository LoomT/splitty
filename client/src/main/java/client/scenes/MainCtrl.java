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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl {

    private Stage primaryStage;

    //private QuoteOverviewCtrl overviewCtrl;
    private Scene overview;

    //private AddQuoteCtrl addCtrl;
    private Scene add;

    private StartScreenCtrl startScreenCtrl;
    private Scene startScreen;

    /**
     * Initializes the UI
     *
     * @param primaryStage stage
     //* @param overview controller and parent
     //* @param add controller and parent
     * @param startScreen controller and scene
     */
    public void initialize(Stage primaryStage, Pair<StartScreenCtrl, Parent> startScreen) {
        this.primaryStage = primaryStage;
        //this.overviewCtrl = overview.getKey();
        //this.overview = new Scene(overview.getValue());

        //this.addCtrl = add.getKey();
        //this.add = new Scene(add.getValue());

        this.startScreenCtrl = startScreen.getKey();
        this.startScreen = new Scene(startScreen.getValue());

        //showOverview();
        showStartScreen();
        primaryStage.show();
    }

    /**
     * Display start screen
     */
    public void showStartScreen() {
        primaryStage.setTitle(LanguageConf.get("StartScreen.title"));
        primaryStage.setScene(startScreen);
    }

    /**
     * Display overview
     */
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