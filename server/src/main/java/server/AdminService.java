package server;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
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

    public static String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Verifies the input password with the stored password
     *
     * @param inputPassword password read in AdminLoginCtrl
     * @return boolean value
     */
    public static boolean verifyPassword(String inputPassword) {
        String adminServiceAttribute = getAdminPassword();

        return adminServiceAttribute.equals(inputPassword);
    }



}
