package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AdminClient {

    private static final String resourcesFolderPath =
            "./client/src/main/resources/client/adminPassword/";


    /**
     * Reads the admin password from the file
     *
     * @return the admin password
     */

    private static String readAdminPasswordFromFile() {
        String filePath = resourcesFolderPath + "adminPassword.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error reading admin-passwor", e);
        }
    }

    /**
     * Verifies the input password with the stored password
     *
     * @param inputPassword password read in AdminLoginCtrl
     * @return boolean value
     */
    public static boolean verifyPassword(String inputPassword) {
        String storedPassword = readAdminPasswordFromFile();

        return storedPassword.equals(inputPassword);
    }
}
