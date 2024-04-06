package server.api;

import commons.Event;
import commons.Tag;
import commons.WebsocketActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.TagRepository;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/tags")
public class TagController {
    private final EventRepository eventRepo;
    private final TagRepository tagRepo;
    private final SimpMessagingTemplate simp;
    private final AdminController adminController;

    /**
     * @param eventRepo Event repository
     * @param tagRepo tag repository interface
     * @param simp websocket object used to send updates to everyone
     * @param adminController admin controller for sending updates
     */
    @Autowired
    public TagController(EventRepository eventRepo, TagRepository tagRepo,
                                 SimpMessagingTemplate simp, AdminController adminController) {
        this.eventRepo = eventRepo;
        this.tagRepo = tagRepo;
        this.simp = simp;
        this.adminController = adminController;
    }

    /**
     * @param eventID event id
     * @param tag tag
     * @return saved tag
     */
    @PostMapping({"", "/"})
    public ResponseEntity<Tag> addTag(@PathVariable String eventID, @RequestBody Tag tag) {
        try {

            Optional<Event> found = eventRepo.findById(eventID);
            if(found.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (tag == null) {
                return ResponseEntity.badRequest().build();
            }

            tag.setEventID(eventID);
            Tag saved = tagRepo.save(tag);
            update(eventID);
            simp.convertAndSend("/event/" + eventID, saved,
                    Map.of("action", WebsocketActions.ADD_TAG,
                            "type", Tag.class.getTypeName()));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates the last activity date of the specified event
     * and updates the date for long poll in admin controller
     *
     * @param eventID event id
     */
    private void update(String eventID) {
        Event event = eventRepo.getReferenceById(eventID);
        event.setLastActivity(new Date());
        eventRepo.save(event);
        adminController.update();
    }
}
