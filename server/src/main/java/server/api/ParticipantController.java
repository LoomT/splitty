package server.api;

import commons.Event;
import commons.Expense;
import commons.Participant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ParticipantRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/participants")
public class ParticipantController {
    private final ParticipantRepository repo;
    private final EventRepository eventRepo;

    /**
     * Constructor with repository and random number generator injections
     *
     * @param repo Participant repository
     * @param eventRepo Event repository
     */
    public ParticipantController(ParticipantRepository repo,
                                 EventRepository eventRepo) {
        this.repo = repo;
        this.eventRepo = eventRepo;
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
            if(eventRepo.findById(eventID).isEmpty() || repo.findById(partID).isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            if(!eventRepo.findById(eventID).get().hasParticipant(repo.findById(partID).get())){
                return ResponseEntity.status(401).build();
            }
            Optional<Participant> participant = repo.findById(partID);
            return participant.map(ResponseEntity::ok).orElseGet(
                    () -> ResponseEntity.notFound().build());
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
            if (participant == null || participant.getName() == null ||
                    participant.getName().isEmpty() || !(eventRepo.existsById(eventID))) {
                return ResponseEntity.badRequest().build();
            }
            Optional<Event> optionalEvent = eventRepo.findById(eventID);
            if (optionalEvent.isPresent()) {
                Event event = optionalEvent.get();
                event.addParticipant(participant);
                eventRepo.save(event);
                return ResponseEntity.ok(participant);
            }
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Change the name of a participant
     * /api/events/{eventID}/participants/{partID}?newName={name}&newEmail={email}
     *
     * @param eventID     id of the Event
     * @param partID      id of the participant
     * @param participant new participant to replace the old one
     * @return the participant entity with new title.
     * or 401 if the participant is not accessible from the specified event
     */
    @PutMapping("/{partID}")
    public ResponseEntity<Participant> editParticipantById(@PathVariable String eventID,
                                                           @PathVariable long partID,
                                                           @RequestBody Participant participant) {
        try {
            Optional<Event> search = eventRepo.findById(eventID);
            Optional<Participant> optional = repo.findById(partID);
            if (search.isEmpty() || optional.isEmpty())
                return ResponseEntity.status(404).build();
            Event event = search.get();
            Participant oldParticipant = optional.get();

            if (!event.hasParticipant(oldParticipant))
                return ResponseEntity.status(401).build();

            List<Expense> expenses = event.getExpenses();
            for (Expense e : expenses) {
                if (e.getExpenseAuthor().equals(oldParticipant)) {
                    e.setExpenseAuthor(participant);
                }
                if(e.getExpenseParticipants().remove(oldParticipant))
                    e.getExpenseParticipants().add(participant);
            }

            event.deleteParticipant(oldParticipant);
            participant.setParticipantId(partID);
            event.addParticipant(participant);

            eventRepo.save(event);
            return ResponseEntity.ok(repo.getReferenceById(partID));


        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * removes a participant
     *
     * @param partID  id of participant to remove
     * @param eventID id of the Event in which the participant is located at
     * @return status 204 if deleted successfully,
     * 404 if the participant and/or event does not exist or
     * 401 if participant is not part of the accessed event
     */
    @DeleteMapping("/{partID}")
    public ResponseEntity<Event> deleteById(@PathVariable long partID,
                                            @PathVariable String eventID) {
        try {
            Optional<Event> eventFound = eventRepo.findById(eventID);
            Optional<Participant> participantFound = repo.findById(partID);
            if (eventFound.isEmpty() || participantFound.isEmpty())
                return ResponseEntity.notFound().build();
            Event event = eventFound.get();
            Participant participant = participantFound.get();
            if (!event.hasParticipant(participant))
                return ResponseEntity.status(401).build();

            List<Expense> toBeDeleted = event.getExpenses();
            List<Expense> expenses = event.getExpenses();
            for (Expense e : expenses) {
                if (e.getExpenseAuthor().equals(participant)) {
                    toBeDeleted.add(e);
                    continue;
                }
                e.getExpenseParticipants().remove(participant);
            }
            for (Expense e : toBeDeleted) {
                expenses.remove(e);
                //this also deletes the expense the participant authored
                //from the event class
            }
            event.deleteParticipant(participant);
            repo.deleteById(partID);
            eventRepo.save(event);
            return ResponseEntity.status(204).build();


        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
