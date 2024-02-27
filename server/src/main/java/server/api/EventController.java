package server.api;

import commons.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventRepository repo;

    /**
     * @param repo Event repository interface (injection)
     */
    public EventController(EventRepository repo) {
        this.repo = repo;
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
        Optional<Event> event = repo.findById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
        if (event == null || event.getTitle() == null || event.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Event saved = repo.save(event);
        return ResponseEntity.ok(saved);
    }

    /**
     * Deletes an event
     *
     * @param id of event to delete
     * @return status 204 if deleted successfully or 404 if the event does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Event> deleteById(@PathVariable String id) {
        if(repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.status(204).build();
        }
        return ResponseEntity.notFound().build();
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
        Optional<Event> found = repo.findById(id);
        if(found.isPresent()) {
            Event event = found.get();
            event.setTitle(title);
            return ResponseEntity.ok(repo.save(event));
        }
        return ResponseEntity.notFound().build();
    }
}
