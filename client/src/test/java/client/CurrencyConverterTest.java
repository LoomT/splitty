package client;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {

    @Test
    void getExchangeTest() {
        URI uri = null;
        try {
            uri = new URI("localhost:8080/api/testCurrencyConverter");
            String test = uri.getScheme();
        } catch (Exception e) {
            fail();
        }
        CurrencyConverter test = CurrencyConverter.createInstance(uri, "EUR", 1, "src/main/resources/client/currencies.properties");
        test.getExchange();
        assertTrue(test.updateExchange());
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