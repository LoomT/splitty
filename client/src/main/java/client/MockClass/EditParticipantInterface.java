package client.MockClass;

import commons.Event;
import javafx.scene.Scene;

public interface EditParticipantInterface {

    /**
     * Sets up the EditParticipant page for displaying
     * @param e event to be updated.
     *
     */
    void displayEditParticipantsPage(Event e);

    /**
     * initializes shortcuts for page
     * @param editParticipants scene to be added shortcuts into
     */
    void initializeShortcuts(Scene editParticipants);
}
