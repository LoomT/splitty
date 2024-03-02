package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyConverter {

    URI uri;

    CurrencyConverter(URI uri){
        this.uri = uri;
    }


    CurrencyConverter(){
        try{
            this.uri = new URI("https://openexchangerates.org/api/latest.json/&base=EUR");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void getExchange() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://openexchangerates.org/api/latest.json?" +
                        "app_id=4368d26633d149e0b992c5bcdce76270"))
                .GET()
                .header("base", "EUR")
                .build();

    }

}
