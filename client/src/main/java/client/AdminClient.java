package client;



public class AdminClient {


    /**
     * Verifies the input password with the stored password
     *
     * @param inputPassword password read in AdminLoginCtrl
     * @return boolean value
     */
    public static boolean verifyPassword(String inputPassword) {
        String adminServiceAttribute = "123";

        return adminServiceAttribute.equals(inputPassword);
    }
}
