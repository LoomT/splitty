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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/tags")
public class TagController {
    private EventRepository eventRepo;
    private TagRepository tagRepo;
    private SimpMessagingTemplate simp;
    private AdminController adminController;

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
            System.out.println("Received request for eventID: " + eventID);

            Optional<Event> found = eventRepo.findById(eventID);
            if(found.isEmpty()) {
                System.out.println("Event not found for eventID: " + eventID);
                return ResponseEntity.notFound().build();
            }

            Event event = found.get();
            System.out.println("Event found for eventID: " + eventID);

            if (tag == null) {
                System.out.println("Received null tag.");
                return ResponseEntity.badRequest().build();
            }

            tag.setEventID(eventID);
            Tag saved = tagRepo.save(tag);
            update(eventID, tag);
            simp.convertAndSend("/event/" + eventID, saved,
                    Map.of("action", WebsocketActions.ADD_TAG,
                            "type", Tag.class.getTypeName()));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates the last activity date of the specified event
     * and updates the date for long poll in admin controller
     *
     * @param eventID event id
     */
    private void update(String eventID, Tag tag) {
        Event event = eventRepo.getReferenceById(eventID);
        List<Tag> temp = event.getTags();
        temp.add(tag);
        event.setTags(temp);
        eventRepo.save(event);
        adminController.update();
    }
}
