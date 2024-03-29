package client;

import client.utils.ServerUtilsImpl;
import client.utils.UserConfig;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.TestIO;
import utils.TestServerUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {

    TestServerUtils server = new TestServerUtils();
    ServerUtilsImpl serverUtils = new ServerUtilsImpl(new UserConfig(new TestIO("""
            serverURL=//localhost:8080/
            lang=en
            recentEventCodes=hello,there""")));

    CurrencyConverterTest() throws IOException {
    }

    @Test
    void getExchangeTest() {
        //get request via server utils
        List<Properties> response = serverUtils.getMockCC();
        StringBuilder string = new StringBuilder();
        for(Properties p : response){
            string.append(p.toString());
        }
        int x = 0;


//        URI uri = null;
//        try {
//            uri = new URI("localhost:8080/api/testCurrencyConverter");
//            String test = uri.getScheme();
//        } catch (Exception e) {
//            fail();
//        }
//        CurrencyConverter test = CurrencyConverter.createInstance(uri, "EUR", 1, "src/main/resources/client/currencies.properties");
//        test.getExchange();
//        assertTrue(test.updateExchange());
    }

    @Test
    void initiate(){


    }

    @Test
    void invalidUpdate(){
        CurrencyConverter test = CurrencyConverter.getInstance();
        assertTrue(test.updateExchange());
    }
}