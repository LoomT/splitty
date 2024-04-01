package server.api;

import commons.Event;
import commons.WebsocketActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventRepository repo;
    private final RandomGenerator random;
    private final SimpMessagingTemplate simp;
    private final AdminController adminController;

    /**
     * Constructor with repository and random number generator injections
     *
     * @param repo Event repository
     * @param random A random number generator
     * @param simp websocket object used to send updates to everyone
     * @param adminController admin controller for sending updates
     */
    @Autowired
    public EventController(EventRepository repo, RandomGenerator random,
                           SimpMessagingTemplate simp, AdminController adminController) {
        this.repo = repo;
        this.random = random;
        this.simp = simp;
        this.adminController = adminController;
    }

    /**
     * Generates an event ID by generating a stream of random integers
     * and converting them into a string
     *
     * @return a random string of 5 uppercase characters
     */
    private String generateId() {
        return random.ints(97, 123)
                .limit(5)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString().toUpperCase();
    }

    /**
     * Path:
     * /api/events/{id}
     *
     * @param id invite code of event to search
     * @return the found event entity or 404 'not found' response otherwise
     */
    @GetMapping( "/{id}")
    public ResponseEntity<Event> getById(@PathVariable String id) {
        try {
            Optional<Event> event = repo.findById(id);
            return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generates an ID for the event, adds it to the database and sends it back to the client
     *
     * @param event to be saved to the database
     * @return the saved entity with an assigned ID
     */
    @PostMapping({ "", "/" })
    public ResponseEntity<Event> add(@RequestBody Event event) {
        try {
            if (event == null || event.getTitle() == null || event.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            String id;
            do {
                id = generateId();
            } while (repo.existsById(id));
            event.setId(id);
            event.setLastActivity(new Date());
            Event saved = repo.save(event);
            adminController.update();
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes an event
     *
     * @param id of event to delete
     * @return status 204 if deleted successfully or 404 if the event does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Event> deleteById(@PathVariable String id) {
        try {
            if(repo.existsById(id)) {
                repo.deleteById(id);
                adminController.update();
                simp.convertAndSend("/event/" + id, "delete",
                        Map.of("action", WebsocketActions.DELETE_EVENT,
                                "type", String.class.getTypeName()));
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Change the title of the event
     * <br>
     * /api/events/{id}?newTitle={title}
     *
     * @param id id of the event
     * @param title new title
     * @return the event entity with new title
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Event> changeTitleById(@PathVariable String id,
                                                 @RequestParam("newTitle") String title) {
        try {
            Optional<Event> found = repo.findById(id);
            if(found.isPresent()) {
                Event event = found.get();
                event.setTitle(title);
                event.setLastActivity(new Date());
                repo.save(event);
                adminController.update();
                simp.convertAndSend("/event/" + id, title,
                        Map.of("action", WebsocketActions.TITLE_CHANGE,
                                "type", String.class.getTypeName()));
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Use this method for changing the title since Java Response doesn't support patch.
     * @param id id of the Event wanted to be changed
     * @param event event to replace the old event
     * @return 204 if successful, 400 if the input is illegal, 404 if the id cannot be found
     */
    @PostMapping("/{id}")
    public ResponseEntity<Event> changeEvent(@PathVariable String id,
                                                 @RequestBody Event event) {
        try {
            if(event.getTitle() == null
                    || event.getId() == null
                    || !event.getId().equals(id)
                    || id.length() != 5
                    || event.getTitle().length() > 100
                    || event.getTitle().isEmpty())
                return ResponseEntity.badRequest().build();

            Optional<Event> found = repo.findById(id);
            if(found.isEmpty())
                return ResponseEntity.notFound().build();
            event.setLastActivity(new Date());
            repo.save(event);
            adminController.update();
            simp.convertAndSend("/event/" + id, event.getTitle(),
                    Map.of("action", WebsocketActions.TITLE_CHANGE,
                                "type", String.class.getTypeName()));
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
