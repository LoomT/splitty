package server.api;

import commons.Event;
import commons.Expense;
import commons.Participant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.AdminService;
import server.database.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

@RestController
public class AdminController {

    private final EventRepository repo;

    /**
     * Constructor with repository injection
     * @param repo Event repository
     */
    public AdminController(EventRepository repo) {
        this.repo = repo;
    }




    /**
     * Verify the input password
     * @param inputPassword the password to verify
     * @return ResponseEntity
     */

    @PostMapping("/admin/verify")
    public ResponseEntity<String> verifyPassword(@RequestBody String inputPassword) {
        boolean isValid = AdminService.verifyPassword(inputPassword);
        if (isValid) {
            return ResponseEntity.ok("Password is correct.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password.");
        }
    }

    /**
     * Adds the event to the database.
     * Manually reassigns participant instances in expenses to the ones in the participant list
     * because the repository treats them as separate entities otherwise.
     * The ids of participants and expenses get reassigned!
     *
     * @param inputPassword admin password
     * @param event event to import
     * @return imported event
     */
    @PostMapping("/admin/events")
    public ResponseEntity<Event> addEvent(@RequestHeader("Authorization") String inputPassword,
                                          @RequestBody Event event) {
        if(!AdminService.verifyPassword(inputPassword))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            List<Participant> participants = event.getParticipants();
            for(Expense expense : event.getExpenses()) {
                Optional<Participant> newExpenseAuthor = participants.stream()
                        .filter(p -> p.getParticipantId()
                                == expense.getExpenseAuthor().getParticipantId())
                        .findAny();
                if(newExpenseAuthor.isEmpty())
                    throw new MissingResourceException(
                            "Missing participant from participant list",
                            "Participant",
                            Long.toString(expense.getExpenseAuthor().getParticipantId()));
                expense.setExpenseAuthor(newExpenseAuthor.get());

                List<Participant> newParticipants = new ArrayList<>();
                for(Participant old : expense.getExpenseParticipants()) {
                    Optional<Participant> newParticipant = participants.stream()
                            .filter(p -> p.getParticipantId() == old.getParticipantId())
                            .findAny();
                    if(newParticipant.isEmpty())
                        throw new MissingResourceException(
                                "Missing participant from participant list",
                                "Participant", Long.toString(old.getParticipantId()));
                    newParticipants.add(newParticipant.get());
                }
                expense.setExpenseParticipants(newParticipants);
            }
            Event saved = repo.save(event);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Sends an API call to server for events
     * @return all events
     * @param inputPassword the password to verify
     */
    @GetMapping ("admin/events")
    public ResponseEntity<List<Event>> getAll(@RequestHeader("Authorization")
                                                  String inputPassword) {
        boolean isValid = AdminService.verifyPassword(inputPassword);
        if (isValid) {
            return ResponseEntity.ok(repo.findAll());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
