package client.MockClass;

import commons.Event;

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
}
