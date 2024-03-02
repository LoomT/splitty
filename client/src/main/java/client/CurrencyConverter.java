package client;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyConverter {

    URI uri;

    /**
     *
     * @param uri custom uri for dependency injection
     */
    CurrencyConverter(URI uri){
        this.uri = uri;
    }

    /**
     * constructor with a default uri value.
     */
    CurrencyConverter(){
        try{
            this.uri = new URI("https://openexchangerates.org/api/latest.json?" +
                    "app_id=4368d26633d149e0b992c5bcdce76270");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    public void getExchange() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("base", "EUR")
                .build();

        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        FileWriter currencies = new FileWriter(Objects.requireNonNull(CurrencyConverter.class.getClassLoader()
                .getResource("client/currencies.properties")).getPath());
        currencies.write(response.body().toString());
        currencies.close();
    }

    /**
     *
     * @param currency
     * @return
     */
    public static double conversionWithBase(String currency){
        return 0; //TODO
    }
}
