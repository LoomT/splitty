package server.api;

import commons.Event;
import commons.Expense;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ExpenseRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/expenses")
public class ExpenseController {
    private final ExpenseRepository repoExpense;
    private final EventRepository repoEvent;
    private final SimpMessagingTemplate simp;

    /**
     * constructor for expense controller
     *
     * @param repoExpense repo of the Expenses
     * @param repoEvent repo of the Events
     * @param simp websocket object to send messages to event subscribers
     */
    public ExpenseController(ExpenseRepository repoExpense, EventRepository repoEvent,
                             SimpMessagingTemplate simp) {
        this.repoExpense = repoExpense;
        this.repoEvent = repoEvent;
        this.simp = simp;
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
            Optional<Event> optionalEvent = repoEvent.findById(eventID);
            Optional<Expense> optionalExpense = repoExpense.findById(id);
            if(optionalEvent.isEmpty() || optionalExpense.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Event event = optionalEvent.get();
            Expense expense = optionalExpense.get();
            if(!event.hasExpense(expense)) {
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.ok(expense); //status code 200(OK) if found
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
            if (checkForBadExpenseFields(expense) || expense.getExpenseID() != 0) {
                return ResponseEntity.badRequest().build();
            }
            Optional<Event> optionalEvent = repoEvent.findById(eventID);
            if (optionalEvent.isEmpty())
                return ResponseEntity.notFound().build();
            Event event = optionalEvent.get();
            if(!new HashSet<>(event.getParticipants()).containsAll(expense.getExpenseParticipants())
                    || !event.hasParticipant(expense.getExpenseAuthor()))
                return ResponseEntity.badRequest().build();
            Expense saved = repoExpense.save(expense);
            optionalEvent.get().addExpense(saved);
            repoEvent.save(optionalEvent.get());
            simp.convertAndSend("/event/" + eventID, saved,
                    Map.of("action", "addExpense", "type", Expense.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
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
            Optional<Event> optionalEvent = repoEvent.findById(eventID);
            Optional<Expense> optionalExpense = repoExpense.findById(id);
            if(optionalEvent.isEmpty() || optionalExpense.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Event event = optionalEvent.get();
            Expense expense = optionalExpense.get();
            if(!event.hasExpense(expense)) {
                return ResponseEntity.status(401).build();
            }

            event.deleteExpense(expense);
            repoEvent.save(event);
            simp.convertAndSend("/event/" + eventID, id,
                    Map.of("action", "removeExpense", "type", Long.class.getTypeName()));
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
            if(checkForBadExpenseFields(updatedExpense) || updatedExpense.getExpenseID() != id)
                return ResponseEntity.badRequest().build();
            Optional<Event> optionalEvent = repoEvent.findById(eventID);
            Optional<Expense> optionalExpense = repoExpense.findById(id);
            if(optionalEvent.isEmpty() || optionalExpense.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Event event = optionalEvent.get();
            Expense expense = optionalExpense.get();
            if(!event.hasExpense(expense)) {
                return ResponseEntity.status(401).build();
            }
            if(!new HashSet<>(event.getParticipants())
                    .containsAll(updatedExpense.getExpenseParticipants()) ||
                    !event.hasParticipant(updatedExpense.getExpenseAuthor()))
                return ResponseEntity.badRequest().build();
            event.deleteExpense(expense);
            event.addExpense(updatedExpense);
            repoEvent.save(event);
            simp.convertAndSend("/event/" + eventID, updatedExpense,
                    Map.of("action", "updateExpense", "type", Expense.class.getTypeName()));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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

