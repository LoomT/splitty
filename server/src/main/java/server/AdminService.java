package server;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AdminService {

    private final String adminPassword;


    /**
     * Constructor for AdminService
     */
    public AdminService() {
        adminPassword = generateAdminPassword();
    }


    /**
     * Generates a random password for the admin
     *
     * @return the generated password
     */

    static String generateAdminPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String password = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Admin password: " + password);
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



}
