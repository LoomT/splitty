package server.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CurrencyControllerTest {

    @Test
    void get() {
        CurrencyController currencyController = new CurrencyController();
        Date date = new Date();
        String dateString = DateTimeFormatter.ISO_DATE
                .format(date.toInstant().atZone(ZoneOffset.UTC).toLocalDate());
        ResponseEntity<Map<String, Double>> response = currencyController.get(dateString);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Double> rates = response.getBody();
        assertNotNull(rates);
        System.out.println(rates.keySet());
    }

    @Test
    void testDoubles() {
        CurrencyController currencyController = new CurrencyController();
        Date date = new Date();
        String dateString = DateTimeFormatter.ISO_DATE
                .format(date.toInstant().atZone(ZoneOffset.UTC).toLocalDate());
        ResponseEntity<Map<String, Double>> response = currencyController.get(dateString);
        Map<String, Double> rates = response.getBody();
        assert rates != null;
        for(Map.Entry<String, Double> entry : rates.entrySet()) {
            assertEquals(Double.class, entry.getValue().getClass());
        }
    }

}