package client.MockClass;

import client.scenes.PairCollector;
import commons.Event;
import commons.Expense;
import commons.Tag;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public interface MainCtrlInterface {

    /**
     * Initializes the UI
     *
     * @param primaryStage         stage
     * @param pairCollector        collector for all of pairs
     */
    void initialize(
            Stage primaryStage,
            PairCollector pairCollector
    );

    /**
     * Display start screen
     */
    void showStartScreen();

    /**
     * Shows the change
     * @param event current event
     */
    void showEditTitle(Event event);

    /**
     * Display admin login
     */
    void showAdminLogin();

    /**
     * shows the event page
     *
     * @param eventToShow the event to display
     */
    void showEventPage(Event eventToShow);

    /**
     * this method is used to switch back to the event
     * page from the participant/expense editors
     * @param event the event to show
     */
    void goBackToEventPage(Event event);

    /**
     * shows the participant editor page
     *
     * @param eventToShow the event to show the participant editor for
     */
    void showEditParticipantsPage(Event eventToShow);

    /**
     * shows the admin overview
     * @param password admin password
     * @param timeOut time out time in ms
     */
    void showAdminOverview(String password, long timeOut);

    /**
     * Opens the system file chooser to save something
     *
     * @param fileChooser file chooser
     * @return opened file
     */
    File showSaveFileDialog(FileChooser fileChooser);

    /**
     * Opens the system file chooser to open multiple files
     *
     * @param fileChooser file chooser
     * @return selected files
     */
    List<File> showOpenMultipleFileDialog(FileChooser fileChooser);

    /**
     * shows the add/edit expense page
     * @param eventToShow the event to show the participant editor for
     */
    void showAddExpensePage(Event eventToShow);

    /**
     * show the add tag page
     * @param event
     */
    void showAddTagPage(Event event);

    /**
     * Handle editing an expense.
     * @param exp The expense to edit.
     * @param ev The event associated with the expense.
     */
    void handleEditExpense(Expense exp, Event ev);

    /**
     * Disconnects from the server and shows an error
     */
    void handleServerNotFound();

    /**
     * display the statistics page
     * @param event the current event
     */
    void showStatisticsPage(Event event);

    /**
     * Initializes a new stage with options
     * and opens it
     */
    void openOptions();

    /**
     * shows openDebts Page
     * @param event event linked to the page
     */
    void showDebtsPage(Event event);

    /**
     * Shows addCustomTransaction scene
     * @param event event customTransaction is connected to
     */
    void showAddCustomTransaction(Event event);

    /**
     * @param languageChoiceBox method for initializing the language switcher
     */
    void initLangChoiceBox(ComboBox<String> languageChoiceBox);


    /**
     * @param  page boolean for the startPage (true) or the eventPage (false)
     */
    void setStartPage(boolean page);

    void showTagPage(Event event);
}
