package client;

import client.utils.ServerUtilsImpl;
import client.utils.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestIO;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {
    ServerUtilsImpl serverUtils;

    CurrencyConverterTest() throws IOException {
        serverUtils = new ServerUtilsImpl(new UserConfig(new TestIO("""
            serverURL=//localhost:8080/
            lang=en
            recentEventCodes=hello,there""")));
    }

    @BeforeEach
    void setup() throws URISyntaxException {
        // to pass the pipeline, for the real tests make sure to run the server
        if(!serverRunning()) return;

        //clean up the currency test property file before each test
        try (Writer fileWriter = new FileWriter(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
                fileWriter.write("");
        }catch(Exception ignored){}
        CurrencyConverter.removeCC();
    }
    @Test
    void getExchangeTest() throws URISyntaxException {
        // to pass the pipeline, for the real tests make sure to run the server
        if(!serverRunning()) return;

        CurrencyConverter test = CurrencyConverter.createInstance(new URI("http://localhost:8080/api/mockCurrencyConverter")
                ,"EUR", 1, Objects.requireNonNull(CurrencyConverter.
                        class.getClassLoader().getResource("client/currenciesTest.properties")).getPath());
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
    void addCurrencyTest() throws URISyntaxException {
        // to pass the pipeline, for the real tests make sure to run the server
        if(!serverRunning()) return;

        CurrencyConverter test = CurrencyConverter.createInstance(new URI("http://localhost:8080/api/mockCurrencyConverter")
                ,"EUR", 1, Objects.requireNonNull(CurrencyConverter.
                        class.getClassLoader().getResource("client/currenciesTest.properties")).getPath());
        assertTrue(test.updateExchange());
        //creating map without "CUR"
        Map<String, Double> map = null;
        try (Reader fileReader = new FileReader(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
            map = test.initializeCurrencyMap(fileReader);
        }catch(Exception ignored){}
        assert map != null;
        assertNull(map.get("CUR"));
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
    void setBaseTest() throws URISyntaxException {
        // to pass the pipeline, for the real tests make sure to run the server
        if(!serverRunning()) return;

        CurrencyConverter test = CurrencyConverter.createInstance(new URI("http://localhost:8080/api/mockCurrencyConverter")
                ,"EUR", 1, Objects.requireNonNull(CurrencyConverter.
                        class.getClassLoader().getResource("client/currenciesTest.properties")).getPath());
        assertTrue(test.updateExchange());
        try (Reader fileReader = new FileReader(Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currenciesTest.properties")).getPath())){
                test.initializeCurrencyMap(fileReader);
        }catch(Exception ignored){}
       assertTrue(test.setBase("USD"));
        assertEquals(test.getBase(), "USD");
        assertEquals(test.getConversionRate(), 0.5);
    }

    boolean serverRunning() throws URISyntaxException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://localhost:8080/api/mockCurrencyConverter")).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }
}