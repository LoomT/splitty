package client.MockClass;

import client.scenes.EventPageCtrl;
import commons.Event;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EditTitleMock implements EditEventTitleInterface {

    private String editEventTitle;

    /**
     * constructor for mock object
     */
    public EditTitleMock(){

    }

    /**
     *
     * @param s new title
     * change title of the editEventTitleCtrl
     */
    public void changeTitle(String s){
        editEventTitle = s;
    }

    /**
     *
     * @param eventPageCtrl eventPageCtrl linked to the event
     * @param event event to be changed
     * @param stage stage to show
     */
    public void displayEditEventTitle(EventPageCtrl eventPageCtrl, Event event, Stage stage){
        return;
    }

    /**
     * initializes shortcuts for page
     * @param editTitle scene to be added shortcuts into
     */
    public void initializeShortcuts(Scene editTitle){

    }
}
