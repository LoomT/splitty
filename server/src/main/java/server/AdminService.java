package server;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.random.RandomGenerator;

@Component
public class AdminService {

    private final String adminPassword;

    private final RandomGenerator random;


    /**
     * Constructor for AdminService
     * @param random RandomGenerator
     */
    public AdminService(RandomGenerator random) {
        this.random = random;
        adminPassword = generateAdminPassword();
        terminal(adminPassword);
    }


    /**
     * Generates a random password for the admin
     *
     * @return the generated password
     */

    String generateAdminPassword() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String password = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Admin password: " + password);
        System.out.println("Type 'pass' to print the password again");
        return password;
    }

    /**
     * Getter for the admin password
     *
     * @return the admin password
     */

    public String getAdminPassword() {
        return this.adminPassword;
    }

    /**
     * Verifies the input password with the stored password
     *
     * @param inputPassword password read in AdminLoginCtrl
     * @return boolean value
     */
    public boolean verifyPassword(String inputPassword) {
        String adminServiceAttribute = getAdminPassword();

        return adminServiceAttribute.equals(inputPassword);
    }

    /**
     * Listens to terminal input and prints the password
     * when user inputs pass
     *
     * @param password password to print
     */
    private void terminal(String password) {
        Thread.startVirtualThread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String input = reader.readLine();
                    if("pass".equals(input)) System.out.println(password);
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });
    }
}
