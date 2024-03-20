package server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import server.AdminService;

import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class AdminControllerTest {

    private AdminController adminController;

    private AdminService adminService;


    @BeforeEach
    public void setUp() {
        TestRandom random = new TestRandom();
        adminService = new AdminService(random);
        adminController = new AdminController(null, adminService);
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
