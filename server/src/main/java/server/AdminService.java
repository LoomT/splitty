package server;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class AdminService {

    private static final String resourcesFolderPath =
            "./client/src/main/resources/client/adminPassword/";


    /**
     * Generates a random password for the admin
     * Stores it in a txt file in the client part of the project
     *
     */
    public static void generateAndStorePassword() {
        String adminPassword = generateAdminPassword();
        System.out.println("Admin password: " + adminPassword);
        try {
            savePasswordToFile(adminPassword);
        } catch (IOException e) {
            System.err.println("Error writing password to file: " + e.getMessage());
        }
    }

    /**
     * Generates a random password for the admin
     *
     * @return the generated password
     */

    static String generateAdminPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24]; // 192 bits
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }


    /**
     * Saves the password to the file
     *
     *
     * @param adminPassword generated admin password
     * @throws IOException if there is a problem with IO
     */
    private static void savePasswordToFile(String adminPassword) throws IOException {
        try (FileWriter writer = new FileWriter(resourcesFolderPath + "adminPassword.txt")) {
            writer.write(adminPassword);
        }
    }

}
