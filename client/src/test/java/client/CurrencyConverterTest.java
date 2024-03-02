package client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {

    @Test
    void getExchangeTest() {
        try {
            new CurrencyConverter().getExchange();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        CurrencyConverter.readConfig();
    }

    @Test
    void initiate(){
        CurrencyConverter asdf = CurrencyConverter.createInstance();
        System.out.println("CurrencyConverter");
    }
}