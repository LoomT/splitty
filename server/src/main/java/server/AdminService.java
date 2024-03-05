package server;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class AdminService {

    private static final String adminPassword = generateAdminPassword();


    /**
     * Generates a random password for the admin
     *
     * @return the generated password
     */

    static String generateAdminPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Getter for the admin password
     *
     * @return the admin password
     */

    static String getAdminPassword() {
        return adminPassword;
    }



}
