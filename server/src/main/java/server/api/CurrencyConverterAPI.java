package server.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class CurrencyConverterAPI {
    /**
     * @return string representation of exchange rates
     */
    @GetMapping("api/mockCurrencyConverter")
    public String get(){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://openexchangerates.org/api/" + "latest.json?app_id=4368d26633d149e0b992c5bcdce76270")).GET().build();

        HttpResponse response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body().toString();
    }
}
