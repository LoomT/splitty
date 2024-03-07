package server.api;

import commons.Expense;
import commons.Participant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.ExpenseRepository;
import server.database.ParticipantRepository;

import java.util.HashSet;
import java.util.Optional;


@RestController
@RequestMapping("/api/events/{eventID}/participants/{participantID}/expenses")
public class ExpenseController {
    private final ExpenseRepository repoExpense;
    private final ParticipantRepository repoParticipant;

    /**
     * constructor for expense controller
     * @param repoExpense
     * @param repoParticipant
     */
    public ExpenseController(ExpenseRepository repoExpense, ParticipantRepository repoParticipant) {
        this.repoExpense = repoExpense;
        this.repoParticipant = repoParticipant;
    }

    /**
     * retrieves an expense according to its id
     * @param id
     * @return ResponseEntity which contains the expense if found, or 404 Not Found otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getById(@PathVariable long id) {
        try {
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
     * @param expense
     * @return the participant with the corresponding expense
     */
    @PostMapping("")
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense) {
        try {
            //save expense
            Expense savedExpense = repoExpense.save(expense);

            Participant expenseAuthor = savedExpense.getExpenseAuthor();

            if (expenseAuthor != null) {

                //updated expense saved for the author
                expenseAuthor.setAuthoredExpenseSet(new HashSet<>());
                expenseAuthor.addExpense(savedExpense);

                //save the participant
                repoParticipant.save(expenseAuthor);

                //return the expense
                return ResponseEntity.ok().body(savedExpense);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * delete an expense
     * @param id
     * @return status 204 if deleting is successful
     * or 404 if trying to delete an expense that does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Expense> deleteById(@PathVariable long id) {
        try {
            if (!repoExpense.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            repoExpense.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * update the expense
     * @param id
     * @param updatedExpense
     * @return status 200 if updating is successful or 404 if the expense does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable long id,
                                                 @RequestBody Expense updatedExpense) {
        try {
            Expense updated = repoExpense.save(updatedExpense);
            updated.setExpenseID(id);
            return ResponseEntity.ok(updated);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
