package server.api;

import commons.Expense;
import commons.Participant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.ExpenseRepository;
import server.database.ParticipantRepository;

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

            //get the author of the expense
            long authorId = savedExpense.getExpenseAuthor().getParticipantId();
            Optional<Participant> optionalParticipant = repoParticipant.findById(authorId);

            if (optionalParticipant.isPresent()) {
                Participant author = optionalParticipant.get();

                //updated expense saved for the author
                author.addExpense(savedExpense);

                //save the participant
                Participant savedAuthor = repoParticipant.save(author);

                //return the expense
                return ResponseEntity.ok(savedExpense);
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
     * @return status 200zz if updating is successful or 404 if the expense does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable long id,
                                                 @RequestBody Expense updatedExpense) {
        try {
            updatedExpense.setExpenseID(id);
            Expense updated = repoExpense.save(updatedExpense);
            return ResponseEntity.ok(updated);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
