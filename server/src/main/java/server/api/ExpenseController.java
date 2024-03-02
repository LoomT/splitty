package server.api;

import commons.Expense;
import commons.Participant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.ExpenseRepository;
import server.database.ParticipantRepository;

import java.util.Optional;


@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseRepository repo1;
    private final ParticipantRepository repo2;

    /**
     * constructor for expense controller
     * @param repo1
     * @param repo2
     */
    public ExpenseController(ExpenseRepository repo1, ParticipantRepository repo2) {
        this.repo1 = repo1;
        this.repo2 = repo2;
    }

    /**
     * retrieves an expense according to its id
     * @param id
     * @return ResponseEntity which contains the expense if found, or 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getById(@PathVariable long id) {
        try {
            Optional<Expense> optionalExpense = repo1.findById(id);
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
     * @param expense
     * @return the participant with the corresponding expense
     */
    @PostMapping("")
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense) {
        try {
            long authorId = expense.getExpenseAuthor().getParticipantId();
            Optional<Participant> optionalParticipant = repo2.findById(authorId);
            if (optionalParticipant.isPresent()) {
                Participant author = optionalParticipant.get();
                author.addExpense(expense);
                repo2.save(author);
                return ResponseEntity.ok(expense);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * delete an expense
     * @param id
     * @return status 204 if deleting is successful or 404 if the expense does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Expense> deleteById(@PathVariable long id) {
        try {
            repo1.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * update the expense
     * @param id
     * @param updatedExpense
     * @return status 204 if updating is successful or 404 if the expense does not exist
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable long id,
                                                 @RequestBody Expense updatedExpense) {
        try {
            updatedExpense.setExpenseID(id);
            Expense updated = repo1.save(updatedExpense);
            return ResponseEntity.ok(updated);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
