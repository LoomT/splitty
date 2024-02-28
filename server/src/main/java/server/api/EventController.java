package server.api;

import commons.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;

import java.util.Optional;
import java.util.random.RandomGenerator;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventRepository repo;
    private final RandomGenerator random;
    /**
     * @param repo Event repository interface (injection)
     */
    public EventController(EventRepository repo, RandomGenerator random) {
        this.repo = repo;
        this.random = random;
    }

    private String generateId() {
        byte[] bytes = new byte[5];
        random.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] %= 26;
            bytes[i] += 65;
        }
        return new String(bytes);
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
     * @param event to save to the database
     * @return the saved entity with an assigned ID
     * <br><br>
     * returned JSON example:<br>
     * {<br>
     *     "id": 2,<br>
     *     "title": "test",<br>
     *     "participants": [],<br>
     *     "creationDate": "1999-01-01T16:16:58.385+00:00"<br>
     * }
     */
    @PostMapping({ "", "/" })
    public ResponseEntity<Event> add(@RequestBody Event event) {
        try {
            if (event == null || event.getTitle() == null || event.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            System.out.println(event);
            String id;
            do {
                id = Event.generateId();
            } while (repo.existsById(id));
            event.setId(id);
            Event saved = repo.save(event);
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
                return ResponseEntity.status(204).build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Change the title of the event
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
                return ResponseEntity.ok(repo.save(event));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
