package server.api;

import commons.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.AdminService;
import server.database.EventRepository;

import java.util.List;

@RestController
public class AdminController {

    private final EventRepository repo;

    private final AdminService admS;


    /**
     * Constructor with repository injection
     * @param repo Event repository
     * @param admS Admin service
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
     * Sends an API call to server for events
     * @return all events
     * @param inputPassword the password to verify
     */
    @GetMapping ("admin/events")
    public ResponseEntity<List<Event>> getAll(@RequestHeader("Authorization")
                                                  String inputPassword) {
        boolean isValid = admS.verifyPassword(inputPassword);
        if (isValid) {
            return ResponseEntity.ok(repo.findAll());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
