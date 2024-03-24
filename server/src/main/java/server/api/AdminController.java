package server.api;

import commons.Event;
import commons.Expense;
import commons.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import server.AdminService;
import server.database.EventRepository;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@RestController
public class AdminController {

    private final EventRepository repo;
    private final AdminService admS;
    private Date lastChange = new Date();

    /**
     * Constructor with repository injection
     * @param repo Event repository
     * @param admS admin service
     */
    @Autowired
    public AdminController(EventRepository repo, AdminService admS) {
        this.repo = repo;
        this.admS = admS;
    }

    /**
     * Verify the input password
     * @param inputPassword the password to verify
     * @return ResponseEntity
     */

    @PostMapping("/admin/verify")
    public ResponseEntity<String> verifyPassword(@RequestBody String inputPassword) {
        boolean isValid = admS.verifyPassword(inputPassword);
        if (isValid) {
            return ResponseEntity.ok("Password is correct.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password.");
        }
    }

    /**
     * Adds the event to the database.
     * Manually reassigns participant instances in expenses to the ones in the participant list
     * because the repository treats them as separate entities otherwise.<p>
     * The ids of participants and expenses get reassigned!
     *
     * @param inputPassword admin password
     * @param event event to import
     * @return
     * Returns 200 ok with saved event in body if successful<p>
     * Returns 400 bad request if a participant in an expense
     * is missing from the participant list<p>
     * Returns 401 unauthorized if password is incorrect<p>
     * Returns 409 conflict if an event with the same id already exists<p>
     * Returns 500 server error if something terrible happens<p>
     */
    @PostMapping("/admin/events")
    public ResponseEntity<Event> addEvent(@RequestHeader("Authorization") String inputPassword,
                                          @RequestBody Event event) {
        if(!admS.verifyPassword(inputPassword))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            HttpStatus status = checkEventValidity(event);
            if(!status.is2xxSuccessful()) return ResponseEntity.status(status).build();

            List<Expense> expenses = event.getExpenses();
            List<Participant> participants = event.getParticipants();
            event.setExpenses(new ArrayList<>());
            Event saved = repo.save(event);
            List<Participant> savedParticipants = saved.getParticipants();
            Map<Long, Long> map = new HashMap<>();
            for(int i = 0; i < participants.size(); i++) {
                map.put(participants.get(i).getId(), savedParticipants.get(i).getId());
                map.put(savedParticipants.get(i).getId(), savedParticipants.get(i).getId());
            }
            for(Expense expense : expenses) {
                expense.getExpenseAuthor().setId(map.get(expense.getExpenseAuthor().getId()));
                for(Participant expenseParticipant : expense.getExpenseParticipants()) {
                    expenseParticipant.setId(map.get(expenseParticipant.getId()));
                }
            }
            saved.getExpenses().addAll(expenses);
            saved = repo.save(saved);
            update();
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * @param event event to check
     * @return 200 if event is valid, 400 if not
     * and 406 if there already exists an event with the same id
     */
    private HttpStatus checkEventValidity(Event event) {
        if(repo.existsById(event.getId()))
            return HttpStatus.CONFLICT;
        Set<Long> participantIds = event.getParticipants().stream()
                .map(Participant::getId).collect(Collectors.toSet());
        for(Expense expense : event.getExpenses()) {
            if(!participantIds.contains(expense.getExpenseAuthor().getId()))
                return HttpStatus.BAD_REQUEST;
            for(Participant expenseParticipant : expense.getExpenseParticipants()) {
                if(!participantIds.contains(expenseParticipant.getId()))
                    return HttpStatus.BAD_REQUEST;
            }
        }
        return HttpStatus.OK;
    }

    /**
     * Sends an API call to server for events
     * @return all events
     * @param inputPassword the password to verify
     */
    @GetMapping ("/admin/events")
    public ResponseEntity<List<Event>> getAll(@RequestHeader("Authorization")
                                                  String inputPassword) {
        boolean isValid = admS.verifyPassword(inputPassword);
        if (isValid) {
            return ResponseEntity.ok(repo.findAll());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /**
     * @param inputPassword admin password
     * @param timeOut millisecond after which send a time-out response
     * @return 200 if there is a change, 408 if time-outed
     */
    @GetMapping("/admin/events/poll")
    public DeferredResult<ResponseEntity<String>>
        longPoll(@RequestHeader("Authorization") String inputPassword,
                 @RequestHeader("TimeOut") Long timeOut) {
        DeferredResult<ResponseEntity<String>> output = new DeferredResult<>(timeOut,
                ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build());
        if(!admS.verifyPassword(inputPassword)) {
            output.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
            return output;
        }

        output.onError((Throwable t) ->
                output.setErrorResult(ResponseEntity.internalServerError().build()));
        Date startTime = new Date();
        ForkJoinPool.commonPool().submit(() -> {
            try {
                while(startTime.after(lastChange)) {
                    if(output.isSetOrExpired()) {
                        return;
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            output.setResult(ResponseEntity.noContent().build());
        });
        return output;
    }

    /**
     * Register new event update
     */
    public void update() {
        lastChange = new Date();
    }
}
