package server.api;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockCurrencyConverterAPI {
    /**
     * @return hardcoded api response to test currency converter
     */
    @GetMapping("api/mockCurrencyConverter")
    public ResponseEntity<String> get(){
        //string to mimic real api response from openExchangeRates so all the methods work correctly
        String jsonResponse = """
                {
                 {
                 "disclaimer": "Usage subject to terms: https://not-a-real-website.org/terms",
                 "license": "https://not-a-real-website.org/license",
                 "timestamp": 1711807220,
                 "base": "USD",
                 "rates": {
                 "USD": 1,
                 "EUR": 2,
                 "CHF": 3,
                 "GBP": 4
                 }
                }""";
        return ResponseEntity.ok().body(jsonResponse);
    }
}
