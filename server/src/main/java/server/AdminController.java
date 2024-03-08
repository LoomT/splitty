package server;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    /**
     * Get the admin password
     * @return the admin password
     */

    @GetMapping("/admin/password")
    public ResponseEntity<String> getAdminPassword() {
        return ResponseEntity.ok(AdminService.getAdminPassword());
    }


    /**
     * Verify the input password
     * @param inputPassword
     * @return ResponseEntity
     */

    @PostMapping("/admin/verify")
    public ResponseEntity<String> verifyPassword(@RequestParam String inputPassword) {
        boolean isValid = AdminService.verifyPassword(inputPassword);
        if (isValid) {
            return ResponseEntity.ok("Password is correct.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password.");
        }
    }
}
