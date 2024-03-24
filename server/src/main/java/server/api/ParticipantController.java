package server.api;

import commons.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ParticipantRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/participants")
public class ParticipantController {
    private final ParticipantRepository repo;
    private final EventRepository eventRepo;
    private final SimpMessagingTemplate simp;
    private final AdminController adminController;

    /**
     * Constructor with repository and random number generator injections
     *
     * @param repo Participant repository
     * @param eventRepo Event repository
     * @param simp websocket object used to send updates to everyone
     * @param adminController admin controller for sending updates
     */
    public ParticipantController(ParticipantRepository repo,
                                 EventRepository eventRepo,
                                 SimpMessagingTemplate simp, AdminController adminController) {
        this.repo = repo;
        this.eventRepo = eventRepo;
        this.simp = simp;
        this.adminController = adminController;
    }

    /**
     * Path:
     * /api/events/{eventID}/participants/{partID}
     *
     * @param eventID invite code of event to search
     * @param partID  id of participant to search for
     * @return the requested participant entity or else a 404 'not found' response
     */
    @GetMapping( "/{partID}")
    public ResponseEntity<Participant> getById(@PathVariable long partID,
                                               @PathVariable String eventID){
        try{
            Optional<Participant> optionalParticipant =
                    repo.findById(new EventWeakKey(eventID, partID));

            return optionalParticipant.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Path:
     * /api/events/{eventID}/participants
     * Generates an ID for the participant, adds it to the database and sends it back to the client
     *
     * @param participant to be saved to the database
     * @param eventID id to which the participant is to be added
     * @return the saved entity with an assigned ID
     */
    @PostMapping({ "", "/" })
    public ResponseEntity<Participant> add(@RequestBody Participant participant,
                                           @PathVariable String eventID) {
        try {
            if(!eventRepo.existsById(eventID)) return ResponseEntity.notFound().build();
            if (participant == null || participant.getName() == null ||
                    participant.getName().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            participant.setEventID(eventID);
            Participant saved = repo.save(participant);
            adminController.update();
            simp.convertAndSend("/event/" + eventID, saved,
                    Map.of("action", WebsocketActions.ADD_PARTICIPANT,
                            "type", Participant.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Change the name of a participant
     * /api/events/{eventID}/participants/{partID}?newName={name}&newEmail={email}
     *
     * @param eventID id of the Event
     * @param partID id of the participant
     * @param participant new participant to replace the old one
     * @return the participant entity with new title.
     *  or 401 if the participant is not accessible from the specified event
     */
    @PutMapping("/{partID}")
    public ResponseEntity<Participant> editParticipantById(@PathVariable String eventID,
                                                           @PathVariable long partID,
                                                           @RequestBody Participant participant) {
        try {
            if (participant == null || participant.getName() == null
                    || participant.getName().isEmpty()
                    || participant.getId() != partID
                    || !eventID.equals(participant.getEventID())) {
                return ResponseEntity.badRequest().build();
            }

            if(!repo.existsById(new EventWeakKey(eventID, partID)))
                return ResponseEntity.notFound().build();

            repo.save(participant);
            adminController.update();
            simp.convertAndSend("/event/" + eventID, participant,
                    Map.of("action", WebsocketActions.UPDATE_PARTICIPANT,
                            "type", Participant.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * removes a participant
     *
     * @param partID id of participant to remove
     * @param eventID id of the Event in which the participant is located at
     * @return status 204 if deleted successfully,
     * 404 if the participant and/or event does not exist or
     * 401 if participant is not part of the accessed event
     */
    @DeleteMapping("/{partID}")
    public ResponseEntity<Event> deleteById(@PathVariable long partID,
                                            @PathVariable String eventID) {
        try {
            Optional<Participant> optionalParticipant =
                    repo.findById(new EventWeakKey(eventID, partID));
            if(optionalParticipant.isEmpty()) return ResponseEntity.notFound().build();

            Participant participant = optionalParticipant.get();
            Event event = eventRepo.getReferenceById(eventID);

            List<Expense> expenses = event.getExpenses();
            expenses.removeIf(expense -> expense.getExpenseAuthor().equals(participant));
            for (Expense e : expenses) {
                e.getExpenseParticipants().remove(participant);
            }

            repo.delete(participant);
            adminController.update();
            simp.convertAndSend("/event/" + eventID, partID,
                    Map.of("action", WebsocketActions.REMOVE_PARTICIPANT,
                            "type", Long.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
