package client;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyConverter {

    private static Map<String, Double> currencyMap;
    private URI apiURI;
    private String base;
    private double conversionRate;
    private String path;


    /**
     * @param apiURI custom uri for dependency injection
     *                             TODO add: double conversionRate
     */
    public CurrencyConverter(URI apiURI, String base, double conversionRate, String path) {
        this.apiURI = apiURI;
        this.base = base;
        this.conversionRate = conversionRate;
        this.path = path;
        try(Reader fileReader = new FileReader(path)) {
            currencyMap = initializeCurrencyMap(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * default constructor for the CurrencyConverter class
     */
    public CurrencyConverter(){
        String path = Objects.requireNonNull(CurrencyConverter.class
                .getClassLoader().getResource("client/migration.properties")).getPath();
        this.path = path;
        this.base = "EUR";
        try(Reader fileReader = new FileReader(path)) {
            currencyMap = initializeCurrencyMap(fileReader);
            this.apiURI = new URI("https://openexchangerates.org/api/" +
                    "latest.json?app_id=4368d26633d149e0b992c5bcdce76270");
            this.conversionRate = 1;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Double> initializeCurrencyMap(Reader fileReader) {
        List<String> temp = new BufferedReader(fileReader).lines().toList();
        Map<String, Double> result = new HashMap<>();

        for (int i = 2; i < temp.size() - 1; i++) {
            String[] tempArr = temp.get(i).split("=");
            result.put(tempArr[0], Double.parseDouble(tempArr[1]));
        }
        return result;
    }

    /**
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    public String getExchange(){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(apiURI)
                .GET()
                .build();

        HttpResponse response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body().toString();
    }

    public boolean updateExchange(){
        String path = Objects.requireNonNull(CurrencyConverter.class.getClassLoader()
                .getResource("client/migration.properties")).getPath();
        String response = getExchange();
        if(response == null) return false;
        List<String> propertiesList = new BufferedReader(new StringReader(response)).lines().toList();

        try(OutputStream outputstream = new FileOutputStream(path)) {
            Properties prop = new Properties();
            for(int i = 7; i<propertiesList.size()-2; i++){
                String[] tempArr = propertiesList.get(i).split(": ");
                prop.setProperty(tempArr[0].replaceAll("[, " + (char) 34 + "]", ""),
                        tempArr[1].replaceAll("[, ]", ""));
            }
            prop.setProperty("base", base);
        prop.store(outputstream, "Test");

        }catch(IOException e){
            return false;
        }
        return true;
    }

    public boolean setBase(String base) {
        if (base == null || !currencyMap.containsKey(base)) {
            return false;
        }
        this.base = base;
        this.conversionRate = currencyMap.get(base) / currencyMap.get("EUR");
        return true;
    }

}
