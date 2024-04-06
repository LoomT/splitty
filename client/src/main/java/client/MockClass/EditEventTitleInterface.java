package client.MockClass;

import client.scenes.EventPageCtrl;
import commons.Event;
import javafx.stage.Stage;

public interface EditEventTitleInterface {

    /**
     *
     * @param title
     * change title of the editEventTitleCtrl
     */
    void changeTitle(String title);

    /**
     *
     * @param eventPageCtrl eventPageCtrl linked to the event
     * @param event event to be changed
     * @param stage stage to show
     */
    void displayEditEventTitle(EventPageCtrl eventPageCtrl, Event event, Stage stage);
}
