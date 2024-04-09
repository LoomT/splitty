package client.utils.currency;

import client.utils.LanguageConf;
import client.utils.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.FileManagerMock;
import utils.TestIO;
import utils.TestServerUtils;
import utils.TestWebsocket;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class CurrencyConverterTest {

    TestServerUtils server;
    FileManagerMock fileManager;
    CurrencyConverter converter;

    @BeforeEach
    void setup() throws IOException {
        server = new TestServerUtils(new TestWebsocket());
        fileManager = new FileManagerMock();
        UserConfig userConfig = new UserConfig(new TestIO("""
                                serverURL=http://localhost:8080/
                                lang=en
                                recentEventCodes=
                                currency=EUR"""));
        LanguageConf languageConf = new LanguageConf(userConfig);
        converter = new CurrencyConverter(server, fileManager, languageConf);
    }

    @Test
    void testAvailableCurrencies() {
        assertEquals(List.of("EUR", "GBP", "USD", "CHF", "JPY"), converter.getCurrencies());
    }

    @Test
    void conversionBothWays() throws IOException {
        double original = 10;
        double converted = converter.convert("EUR", "USD", original, Instant.EPOCH);
        converted = converter.convert("USD", "EUR", converted, Instant.EPOCH);
        assertEquals(original, converted, 0.001);
    }

    @Test
    void newCache() throws IOException {
        assertEquals(0, fileManager.getCache().size());
        assertFalse(server.getCalls().contains("getExchangeRates"));
        converter.convert("EUR", "USD", 10, Instant.EPOCH);
        assertEquals(1, fileManager.getCache().size());
        assertTrue(server.getCalls().contains("getExchangeRates"));
    }

    @Test
    void usingCache() throws IOException {
        converter.convert("EUR", "USD", 10, Instant.EPOCH);
        assertEquals(1, fileManager.getCache().size());
        converter.convert("EUR", "USD", 10, Instant.EPOCH);
        assertEquals(1, fileManager.getCache().size());
        assertEquals(1, server.getCalls().stream().filter(c -> c.contains("getExchangeRates")).count());
    }

    @Test
    void usingSameCacheForDifferentCurrencies() throws IOException {
        converter.convert("JPY", "CHF", 10, Instant.EPOCH);
        assertEquals(1, fileManager.getCache().size());
        converter.convert("EUR", "USD", 10, Instant.EPOCH);
        assertEquals(1, fileManager.getCache().size());
    }
}