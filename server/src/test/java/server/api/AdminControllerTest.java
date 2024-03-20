package server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import server.AdminService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class AdminControllerTest {

    private AdminController adminController;

    @BeforeEach
    public void setUp() {
        adminController = new AdminController(null, new AdminService());
    }

    @Test
    public void testVerifyPasswordOk() {
        String testPassword = adminController.getAdmS().getAdminPassword();

        ResponseEntity<String> response = adminController.verifyPassword(testPassword);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password is correct.", response.getBody());
    }

    @Test
    public void testVerifyPasswordUnauthorized() {
        String incorrectPassword = "12345";

        ResponseEntity<String> response = adminController.verifyPassword(incorrectPassword);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Incorrect password.", response.getBody());
    }
}
