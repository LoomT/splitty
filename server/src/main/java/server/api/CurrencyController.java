package server.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CurrencyController {

    /**
     * @param date date of format yyyy-mm-dd
     * @return string representation of exchange rates
     */
    @SuppressWarnings("unchecked")
    @GetMapping("api/currency/{date}")
    public ResponseEntity<Map<String, Double>> get(@PathVariable String date){
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            String url = "https://openexchangerates.org/api/historical/";
            String end = ".json?app_id=4368d26633d149e0b992c5bcdce76270";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + date + end)).GET().build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                return ResponseEntity.status(response.statusCode()).build();
            }
            ObjectReader reader = new ObjectMapper().reader().forType(Map.class);
            Map<String, Object> map = reader.readValue(response.body());

            Map<String, Object> rates = (Map<String, Object>) map.get("rates");
            Map<String, Double> fixedRates = new HashMap<>();
            for(Map.Entry<String, Object> entry : rates.entrySet()){
                if(entry.getKey().equals("BTC")) continue;
                fixedRates.put(entry.getKey(), Double.valueOf(String.valueOf(entry.getValue())));
            }
            return ResponseEntity.ok(fixedRates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
