package client;

import client.utils.CurrencyConverter;
import client.utils.ServerUtilsImpl;
import client.utils.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestIO;
import utils.TestServerUtils;
import utils.TestWebsocket;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

import static client.utils.CurrencyConverter.toMonth;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {
    TestServerUtils serverUtils;

    //ServerUtilsImpl realTest = new ServerUtilsImpl(new UserConfig(new FileIO(URI.create("config.properties").toURL())));

    CurrencyConverterTest() {
        serverUtils = new TestServerUtils(new TestWebsocket());
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
    }

    @Test
    void toMonthTest(){
        String january = "Jan";
        String february = "Feb";
        String march = "Mar";
        String april = "Apr";
        String may = "May";
        String june = "Jun";
        String july = "Jul";
        String august = "Aug";
        String september = "Sep";
        String october = "Oct";
        String november = "Nov";
        String december = "Dec";
        assertEquals(toMonth(january), Calendar.JANUARY);
        assertEquals(toMonth(february), Calendar.FEBRUARY);
        assertEquals(toMonth(march), Calendar.MARCH);
        assertEquals(toMonth(april), Calendar.APRIL);
        assertEquals(toMonth(may), Calendar.MAY);
        assertEquals(toMonth(june), Calendar.JUNE);
        assertEquals(toMonth(july), Calendar.JULY);
        assertEquals(toMonth(august), Calendar.AUGUST);
        assertEquals(toMonth(september), Calendar.SEPTEMBER);
        assertEquals(toMonth(october), Calendar.OCTOBER);
        assertEquals(toMonth(november), Calendar.NOVEMBER);
        assertEquals(toMonth(december), Calendar.DECEMBER);
        assertThrows(RuntimeException.class ,  () -> {toMonth("not_a_real_month");});
    }

    @Test
    void realCurrencyConverterTest(){
        assertEquals("EUR", CurrencyConverter.getInstance().getBase());
        assertFalse(CurrencyConverter.getInstance().setBase(null));
    }

    @Test
    void runtimeExceptionTest(){
        assertThrows(RuntimeException.class ,  () -> {CurrencyConverter.createInstance("EUR", 1, "not_a_real_path", new ServerUtilsImpl(new UserConfig(new TestIO(""))));});
    }
}