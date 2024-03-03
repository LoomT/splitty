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

import static com.google.inject.Guice.createInjector;

import java.io.IOException;
import java.net.URISyntaxException;

import client.scenes.EventPageCtrl;
import client.scenes.StartScreenCtrl;
import client.utils.LanguageConf;
import com.google.inject.Injector;

import client.scenes.MainCtrl;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    /**
     * Main class
     *
     * @param args Runtime arguments
     * @throws URISyntaxException if there is a URI syntax error
     * @throws IOException        if there is a problem with IO
     */
    public static void main(String[] args) throws URISyntaxException, IOException {
        launch();
    }

    /**
     * Starts the application
     *
     * @param primaryStage stage
     * @throws IOException IO exception
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        LanguageConf.onLanguageChange(() -> {
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
    public void loadLanguageResourcesAndStart(Stage primaryStage) {
        // Load all the FXML here:

        //var overview = FXML.load(QuoteOverviewCtrl.class,
        // "client", "scenes", "QuoteOverview.fxml");
        //var add = FXML.load(AddQuoteCtrl.class, "client", "scenes", "AddQuote.fxml");
        var start = FXML.load(
                StartScreenCtrl.class,
                LanguageConf.getLanguageResources(),
                "client", "scenes", "StartScreen.fxml"
        );

        var eventPage = FXML.load(
                EventPageCtrl.class,
                LanguageConf.getLanguageResources(),
                "client", "scenes", "EventPage.fxml"
        );


        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, start, eventPage);
    }
}