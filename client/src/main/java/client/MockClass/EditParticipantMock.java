package client.MockClass;

import commons.Event;
import javafx.scene.Scene;

public class EditParticipantMock implements EditParticipantInterface {

    private Event event;

    /**
     * Constructor for EditParticipantMock class
     */
    public EditParticipantMock(){
    }

    /**
     * Sets up the EditParticipant page for displaying
     * @param e event to be updated.
     */
    public void displayEditParticipantsPage(Event e){
        event = e;
    }

    /**
     * initializes shortcuts for page
     * @param editParticipants scene to be added shortcuts into
     */
    public void initializeShortcuts(Scene editParticipants) {

    }
}
