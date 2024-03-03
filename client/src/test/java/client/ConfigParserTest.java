package client;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {


    /**
     * Assert that the parser is created
     */
    @Test
    void createInstance() throws IOException {
        assertNotNull(ConfigParser.createInstance());
    }

    /**
     * Assert that it initializes with the correct URL
     */
    @Test
    void getUrl() throws IOException {
        String absolutePath = new File("").getAbsolutePath();
        System.out.println("tha path is " + absolutePath);
        File file = new File(absolutePath + "/src/main/resources/client/config.properties");
        ConfigParser parser = ConfigParser.createInstance();
        assertEquals(new Scanner(file).nextLine(), parser.getUrl());
    }
}