package server.api;

import commons.Event;
import commons.EventWeakKey;
import commons.Expense;
import commons.WebsocketActions;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ExpenseRepository;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/expenses")
public class ExpenseController {
    private final ExpenseRepository repoExpense;
    private final EventRepository eventRepo;
    private final SimpMessagingTemplate simp;
    private final AdminController adminController;

    /**
     * constructor for expense controller
     *
     * @param repoExpense repo of the Expenses
     * @param eventRepo repo of events
     * @param simp websocket object to send messages to event subscribers
     * @param adminController admin controller for sending updates
     */
    public ExpenseController(ExpenseRepository repoExpense, EventRepository eventRepo,
                             SimpMessagingTemplate simp, AdminController adminController) {
        this.repoExpense = repoExpense;
        this.eventRepo = eventRepo;
        this.simp = simp;
        this.adminController = adminController;
    }

    /**
     * retrieves an expense according to its id
     *
     * @param id id of the expense
     * @param eventID ID of the event expense is a part of
     * @return ResponseEntity which contains the expense if found, or 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getById(@PathVariable long id,
                                           @PathVariable String eventID) {
        try {
            Optional<Expense> optionalExpense = repoExpense.findById(new EventWeakKey(eventID, id));
            //status code 200(OK) if found
            return optionalExpense.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * find participant, add the expense to it and save the participant
     *
     * @param expense expense to be added
     * @param eventID ID of the event expense is a part of
     * @return the participant with the corresponding expense
     */
    @PostMapping({"", "/"})
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense,
                                              @PathVariable String eventID) {
        try {
            if(!eventRepo.existsById(eventID)) return ResponseEntity.notFound().build();
            if (checkForBadExpenseFields(expense)) {
                return ResponseEntity.badRequest().build();
            }

            expense.setEventID(eventID);
            Expense saved = repoExpense.save(expense);
            update(eventID);
            simp.convertAndSend("/event/" + eventID, saved,
                    Map.of("action", WebsocketActions.ADD_EXPENSE,
                            "type", Expense.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * delete an expense
     *
     * @param id id of the expense
     * @param eventID event expense is a part of
     * @return status 204 if deleting is successful
     * or 404 if trying to delete an expense that does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Expense> deleteById(@PathVariable long id,
                                              @PathVariable String eventID) {
        try {
            Optional<Expense> optionalExpense = repoExpense.findById(new EventWeakKey(eventID, id));
            if(optionalExpense.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            repoExpense.delete(optionalExpense.get());
            update(eventID);
            simp.convertAndSend("/event/" + eventID, id,
                    Map.of("action", WebsocketActions.REMOVE_EXPENSE,
                            "type", Long.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * update the expense
     *
     * @param id id of the expense to be updated
     * @param updatedExpense updated version of the expense
     * @param eventID ID of the event
     * @return status 200 if updating is successful or 404 if the expense does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable long id,
                                                 @RequestBody Expense updatedExpense,
                                                 @PathVariable String eventID) {
        try {
            if(checkForBadExpenseFields(updatedExpense)
                    || updatedExpense.getId() != id
                    || !updatedExpense.getEventID().equals(eventID))
                return ResponseEntity.badRequest().build();

            if(!repoExpense.existsById(new EventWeakKey(eventID, id)))
                return ResponseEntity.notFound().build();

            repoExpense.save(updatedExpense);
            update(eventID);
            simp.convertAndSend("/event/" + eventID, updatedExpense,
                    Map.of("action", WebsocketActions.UPDATE_EXPENSE,
                            "type", Expense.class.getTypeName()));
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

    /**
     * @param expense expense to check
     * @return true iff any required fields are null or empty
     */
    private boolean checkForBadExpenseFields(Expense expense) {
        return expense == null || expense.getExpenseAuthor() == null
                || expense.getPurpose() == null || expense.getPurpose().isEmpty()
                || expense.getCurrency() == null || expense.getCurrency().isEmpty()
                || expense.getExpenseParticipants() == null
                || expense.getType() == null || expense.getType().isEmpty();
    }
}

