package server.api;

import commons.Event;
import commons.Expense;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.AdminService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class AdminControllerTest {

    private AdminController adminController;

    private AdminService adminService;


    @BeforeEach
    public void setUp() {
        TestRandom random = new TestRandom();
        adminService = new AdminService(random);
        TestParticipantRepository partRepo = new TestParticipantRepository();
        TestExpenseRepository expRepo = new TestExpenseRepository();
        TestEventRepository eventRepo = new TestEventRepository(partRepo, expRepo);
        partRepo.setEventRepo(eventRepo);
        expRepo.setEventRepo(eventRepo);
        adminController = new AdminController(eventRepo, adminService);
    }

    @Test
    public void testVerifyPasswordOk() {
        String testPassword = adminService.getAdminPassword();

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

    @Test
    public void testImportEvent() {
        Participant participant1 = new Participant("1");
        Participant participant2 = new Participant("2");
        participant1.setId(1);
        participant2.setId(2);
        participant1.setEventID("ABCDE");
        participant2.setEventID("ABCDE");
        Expense expense = new Expense(participant1, "no", 5, "eur",
                new ArrayList<>(List.of(participant1, participant2)), "food");
        expense.setEventID("ABCDE");
        Event event = new Event("title", new ArrayList<>(List.of(participant1, participant2)),
                new ArrayList<>(List.of(expense)));
        event.setId("ABCDE");

        ResponseEntity<Event> response = adminController.addEvent(adminService.getAdminPassword(), event);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Event saved = response.getBody();
        assertEquals("ABCDE", saved.getId());
        assertEquals(2, saved.getParticipants().size());
        assertEquals(1, saved.getExpenses().size());
    }
}
