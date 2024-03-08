package server.api;

import commons.Event;
import commons.Expense;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ExpenseRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/events/{eventID}/expenses")
public class ExpenseController {
    private final ExpenseRepository repoExpense;
    private final EventRepository repoEvent;

    /**
     * constructor for expense controller
     *
     * @param repoExpense repo of the Expenses
     * @param repoEvent repo of the Events
     */
    public ExpenseController(ExpenseRepository repoExpense, EventRepository repoEvent) {
        this.repoExpense = repoExpense;
        this.repoEvent = repoEvent;
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
            if(repoEvent.findById(eventID).isEmpty() || repoExpense.findById(id).isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            if(!repoEvent.findById(eventID).get().hasExpense(repoExpense.findById(id).get())){
                return ResponseEntity.status(401).build();
            }
            Optional<Expense> optionalExpense = repoExpense.findById(id);
            if (optionalExpense.isPresent()) {
                return ResponseEntity.ok(optionalExpense.get()); //status code 200(OK) if found
            } else {
                return ResponseEntity.notFound().build(); //status code 404(Not Found) if not found
            }
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
    @PostMapping("")
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense,
                                              @PathVariable String eventID) {
        try {
            if (expense == null || expense.getExpenseAuthor() == null
                    || expense.getPurpose() == null || expense.getPurpose().isEmpty()
                    || expense.getCurrency() == null || expense.getCurrency().isEmpty()
                    || expense.getDate() == null || expense.getExpenseParticipants() == null
                    || expense.getType() == null || expense.getType().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            Optional<Event> optionalEvent = repoEvent.findById(eventID);
            if (optionalEvent.isEmpty())
                return ResponseEntity.internalServerError().build();

            optionalEvent.get().addExpense(expense);
            repoEvent.save(optionalEvent.get());
            return ResponseEntity.ok(expense);
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
            Optional<Event> eventFound = repoEvent.findById(eventID);
            Optional<Expense> expenseFound = repoExpense.findById(id);
            if(eventFound.isEmpty() || expenseFound.isEmpty())
                return ResponseEntity.notFound().build();

            Event event = eventFound.get();
            Expense expense = expenseFound.get();
            if(!event.hasExpense(expense))
                return ResponseEntity.notFound().build(); //I need to check this

            event.deleteExpense(expense);
            repoExpense.deleteById(id);
            repoEvent.save(event);
            return ResponseEntity.status(204).build();
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
            Optional<Event> eventFound = repoEvent.findById(eventID);
            Optional<Expense> expenseFound = repoExpense.findById(id);
            if(eventFound.isEmpty() || expenseFound.isEmpty()
                    || !eventFound.get().hasExpense(expenseFound.get()))
                return ResponseEntity.notFound().build();

            Event event = eventFound.get();
            Expense expense = expenseFound.get();

            event.deleteExpense(expense);
            event.addExpense(updatedExpense);
            updatedExpense.setExpenseID(id);
            repoEvent.save(event);

            return ResponseEntity.status(204).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}

