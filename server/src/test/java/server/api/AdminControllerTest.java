package server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.ResponseEntity;
import server.AdminService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

@SuppressWarnings("deprecation")
public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void testVerifyPasswordOk() {
        String testPassword = AdminService.getAdminPassword();

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
