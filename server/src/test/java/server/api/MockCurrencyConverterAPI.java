package server.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

@RestController
@RequestMapping("/api/testCurrencyConverter")
public class MockCurrencyConverterAPI {
    @GetMapping()
    public ResponseEntity<Properties> get(){
        Properties p = new Properties();
        p.setProperty("EUR", "1");
        p.setProperty("USD", "0.5");
        p.setProperty("Yen", "0.8");
        p.setProperty("Mark", "0.9");
        return ResponseEntity.ok(p);
    }
}
