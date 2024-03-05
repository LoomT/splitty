package server.api;

import commons.Event;
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
     */
    public ParticipantController(ParticipantRepository repo, EventRepository eventRepo) {
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
    public ResponseEntity<Participant> getById(@PathVariable long partID, @PathVariable String eventID){
        try{
            List<Participant> list = repo.findAll();
            long test1 = partID;
            Optional<Participant> test = repo.findById(partID);
            if(eventRepo.findById(eventID).isEmpty() || repo.findById(partID).isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            if(!eventRepo.findById(eventID).get().hasParticipant(repo.findById(partID).get())){
                return ResponseEntity.status(401).build();
            }
            Optional<Participant> participant = repo.findById(partID);
            return participant.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
    public ResponseEntity<Participant> add(@RequestBody Participant participant, @PathVariable String eventID) {
        try {
            boolean test = !(eventRepo.existsById(eventID));
            List<Event> list = eventRepo.findAll();
            if (participant == null || participant.getName() == null || participant.getName().isEmpty() || !(eventRepo.existsById(eventID))) {
                return ResponseEntity.badRequest().build();
            }
            if (eventRepo.findById(eventID).isPresent()) {
                Event event = eventRepo.findById(eventID).get();
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
     * @param partID id of the participant
     * @param name new name for participant
     * @param email new email for participant
     * @return the participant entity with new title.
     *  or 401 if the participant is not accessible from the specified event
     */
    @PatchMapping("/{partID}")
    public ResponseEntity<Participant> editParticipantById(@PathVariable String eventID, @PathVariable long partID,
                                                 @RequestParam("newName") String name, @RequestParam String email) {
        try {
            Optional<Event> search = eventRepo.findById(eventID);
            if(search.isPresent() && repo.existsById(partID)) {
                Event event = search.get();
                Optional<Participant> optional = repo.findById(partID);

                if(optional.isPresent()){
                    Participant participant = optional.get();
                    if(event.hasParticipant(participant)){
                        participant.setName(name);
                        participant.setEmailAddress(email);
                        eventRepo.save(event);
                        return ResponseEntity.ok(repo.getReferenceById(partID));
                    } else return ResponseEntity.status(401).build();
                }
            }

            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * removes a participant
     *
     * @param partID id of participant to remove
     * @return status 204 if deleted successfully, 404 if the participant and/or event does not exist or 401 if participant is not part of the accessed event
     */
    @DeleteMapping("/{partID}")
    public ResponseEntity<Event> deleteById(@PathVariable long partID, @PathVariable String eventID) {
        try {
            if(eventRepo.existsById(eventID)) {
                Event event = eventRepo.getReferenceById(eventID);
                if(event.hasParticipant(repo.getReferenceById(partID))){
                    if (repo.existsById(partID)) {
                        eventRepo.getReferenceById(eventID).deleteParticipant(repo.getReferenceById(partID));
                        repo.deleteById(partID);
                        return ResponseEntity.status(204).build();
                    }
                }
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
