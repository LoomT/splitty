package server.api;

import commons.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.TransactionRepository;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/transactions")
public class TransactionController {
    private final EventRepository eventRepo;
    private final TransactionRepository transactionRepo;
    private final SimpMessagingTemplate simp;
    private final AdminController adminController;

    /**
     * @param eventRepo Event repository
     * @param transactionRepo transaction repository interface
     * @param simp websocket object used to send updates to everyone
     * @param adminController admin controller for sending updates
     */
    @Autowired
    public TransactionController(EventRepository eventRepo, TransactionRepository transactionRepo,
                                 SimpMessagingTemplate simp, AdminController adminController) {
        this.eventRepo = eventRepo;
        this.transactionRepo = transactionRepo;
        this.simp = simp;
        this.adminController = adminController;
    }

    /**
     * @param eventID event id
     * @param transaction transaction
     * @return saved transaction
     */
    @PostMapping({"", "/"})
    public ResponseEntity<Transaction> add(@PathVariable String eventID,
                                     @RequestBody Transaction transaction) {
        try {
            Optional<Event> found = eventRepo.findById(eventID);
            if(found.isEmpty()) return ResponseEntity.notFound().build();
            Event event = found.get();
            if (transaction == null || !event.hasParticipant(transaction.getGiver())
                || !event.hasParticipant(transaction.getReceiver())) {
                return ResponseEntity.badRequest().build();
            }
            transaction.setEventID(eventID);
            Transaction saved = transactionRepo.save(transaction);
            update(eventID);
            simp.convertAndSend("/event/" + eventID, saved,
                    Map.of("action", WebsocketActions.ADD_TRANSACTION,
                            "type", Transaction.class.getTypeName()));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * @param eventID event id
     * @param id transaction id
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Transaction> deleteById(@PathVariable String eventID,
                                                  @PathVariable long id) {
        try {
            EventWeakKey key = new EventWeakKey(eventID, id);
            if(!transactionRepo.existsById(key)) {
                return ResponseEntity.notFound().build();
            }
            transactionRepo.deleteById(key);
            update(eventID);
            simp.convertAndSend("/event/" + eventID, id,
                    Map.of("action", WebsocketActions.REMOVE_TRANSACTION,
                            "type", Long.class.getTypeName()));
            return ResponseEntity.noContent().build();
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
