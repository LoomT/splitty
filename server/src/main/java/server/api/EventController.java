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
     * /api/events?id={id}
     *
     * @param id invite code of event to search
     * @return the found event entity or 404 'not found' response otherwise
     */
    @GetMapping("")
    public ResponseEntity<Event> findById(@RequestParam("id") long id) {
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
    @PostMapping(path = { "", "/" })
    public ResponseEntity<Event> add(@RequestBody Event event) {
        if (event == null || event.getTitle() == null || event.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Event saved = repo.save(event);
        return ResponseEntity.ok(saved);
    }
}
