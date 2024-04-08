package client.utils.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestServerUtils;


class CurrencyConverterTest {

    TestServerUtils serverUtils;

    @BeforeEach
    void setup() {
        serverUtils = new TestServerUtils();
    }

    @Test
    void test() {
        FileManagerImpl fileManager = new FileManagerImpl();
        System.out.println(fileManager.getAvailableCurrencies());
    }
}