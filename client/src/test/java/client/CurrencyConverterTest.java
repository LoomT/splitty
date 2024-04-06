package client;

import client.utils.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestServerUtils;

import java.io.*;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {
    TestServerUtils serverUtils;

    //ServerUtilsImpl realTest = new ServerUtilsImpl(new UserConfig(new FileIO(URI.create("config.properties").toURL())));

    CurrencyConverterTest() {
        serverUtils = new TestServerUtils();
    }

    @BeforeEach
    void setup() {
        //clean up the currency test property file before each test
        try (Writer fileWriter = new FileWriter(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
                fileWriter.write("");
        }catch(Exception ignored){}
        CurrencyConverter.removeCC();
    }
    @Test
    void getExchangeTest() {
        CurrencyConverter test = CurrencyConverter.createInstance("EUR", 1, Objects.requireNonNull(CurrencyConverter.
                        class.getClassLoader().getResource("client/currenciesTest.properties")).getPath(), serverUtils);
        test.getExchange();
        Map<String, Double> map = null;
        try (Reader fileReader = new FileReader(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
            map = test.initializeCurrencyMap(fileReader);
        }catch(Exception ignored){}
        assert map != null;
        assertEquals(map.get("USD"), 1);
        assertEquals(map.get("EUR"), 2);
        assertEquals(map.get("CHF"), 3);
        assertEquals(map.get("GBP"), 4);
    }

    @Test
    void addCurrencyTest() {
        CurrencyConverter test = CurrencyConverter.createInstance("EUR", 1, Objects.requireNonNull(CurrencyConverter.
                        class.getClassLoader().getResource("client/currenciesTest.properties")).getPath(), serverUtils);
        assertTrue(test.updateExchange());
        //creating map without "CUR"
        Map<String, Double> map = null;
        try (Reader fileReader = new FileReader(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
            map = test.initializeCurrencyMap(fileReader);
        }catch(Exception ignored){}
        assert map != null;
        assertNull(map.get("CUR"));
        //adding currency "CUR"
        assertTrue(test.addCurrency("CUR", 5));
        Map<String, Double> mapWithAddedCurr = null;
        try (Reader fileReader = new FileReader(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
            mapWithAddedCurr = test.initializeCurrencyMap(fileReader);
        }catch(Exception ignored){}
        assert mapWithAddedCurr != null;
        assertEquals(5, mapWithAddedCurr.get("CUR"));
    }

    @Test
    void setBaseTest() {
        CurrencyConverter test = CurrencyConverter.createInstance("EUR", 1, Objects.requireNonNull(CurrencyConverter.
                        class.getClassLoader().getResource("client/currenciesTest.properties")).getPath(), serverUtils);
        assertTrue(test.updateExchange());
        try (Reader fileReader = new FileReader(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
                test.initializeCurrencyMap(fileReader);
        }catch(Exception ignored){}
       assertTrue(test.setBase("USD"));
        assertEquals(test.getBase(), "USD");
        assertEquals(test.getConversionRate(), 0.5);
        CurrencyConverter.removeCC();
        CurrencyConverter cur = CurrencyConverter.getInstance();
        cur.updateExchange();
        cur.getExchange();

    }
}