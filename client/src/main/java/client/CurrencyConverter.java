package client;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Currency converter which can scan the currencies.properties file to
 * create a map of currencies and respective exchange rates and fetch
 * exchange rates from openexchangerates API.
 * Uses singleton pattern.
 */
public class CurrencyConverter {

    private static CurrencyConverter currencyConverter;
    private static Map<String, Double> currencyMap;
    private URI apiURI;
    private String base;
    private double conversionRate;
    private String path;


    /**
     * @param apiURI custom uri for dependency injection
     */
    private CurrencyConverter(URI apiURI, String base, double conversionRate, String path) {
        this.apiURI = apiURI;
        this.base = base;
        this.conversionRate = conversionRate;
        this.path = path;
        try (Reader fileReader = new FileReader(path)) {
            currencyMap = initializeCurrencyMap(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * default constructor for the CurrencyConverter class
     */
    private CurrencyConverter() {
        String path = Objects.requireNonNull(CurrencyConverter.
                class.getClassLoader().getResource("client/currencies.properties")).getPath();
        this.path = path;
        this.base = "EUR";
        try (Reader fileReader = new FileReader(path)) {
            this.apiURI = new URI("https://openexchangerates.org/api/" + "latest.json?app_id=4368d26633d149e0b992c5bcdce76270");
            currencyMap = initializeCurrencyMap(fileReader);
            this.conversionRate = 1;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return instance of CurrencyConverter
     */
    public static CurrencyConverter getInstance() {
        if (currencyConverter == null) {
            currencyConverter = new CurrencyConverter();
        }
        return currencyConverter;
    }

    /**
     * Instance creator with variables so that it can be used with testing and dependency injection
     *
     * @param apiURI         URI of the api used
     * @param base           base currency used in the local application
     * @param conversionRate value of base divided by value of Euro
     * @param path           path of config file
     * @return Instance of currency converter
     */
    public static CurrencyConverter createInstance(
            URI apiURI, String base, double conversionRate, String path) {
        if (currencyConverter == null) {
            currencyConverter = new CurrencyConverter(apiURI, base, conversionRate, path);
        }
        return currencyConverter;
    }

    /**
     *
     * @param fileReader reads file to initialize the currencyMap. If the file is empty, it fetches
     * the currencies and initializes them on the config file
     * @return CurrencyMap created with values from the config file
     */
    public Map<String, Double> initializeCurrencyMap(Reader fileReader) {
        List<String> temp = new BufferedReader(fileReader).lines().toList();
        Map<String, Double> result = new HashMap<>();
        if (temp.isEmpty()) updateExchange();

        for (int i = 2; i < temp.size() - 1; i++) {
            String[] tempArr = temp.get(i).split("=");
            result.put(tempArr[0], Double.parseDouble(tempArr[1]));
        }
        return result;
    }

    /**
     * @return a String of the http response.
     */
    public String getExchange() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(apiURI).GET().build();

        HttpResponse response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body().toString();
    }

    /**
     *
     * @return updates the currencies.properties file by fetching
     * up-to-date exchange data from the API.
     */
    public boolean updateExchange() {
        String response = getExchange();
        if (response == null) return false;
        List<String> propertiesList = new BufferedReader(
                new StringReader(response)).lines().toList();

        try (OutputStream outputstream = new FileOutputStream(path)) {
            Properties prop = new Properties();
            for (int i = 7; i < propertiesList.size() - 2; i++) {
                String[] tempArr = propertiesList.get(i).split(": ");
                prop.setProperty(tempArr[0].replaceAll("[, " + (char) 34 + "]", ""),
                        tempArr[1].replaceAll("[, ]", ""));
            }
            prop.setProperty("base", base);
            prop.store(outputstream, "Test");

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param base change the base currency of the user
     * @return true if the base currency can be changed, false otherwise
     */
    public boolean setBase(String base) {
        if (base == null || !currencyMap.containsKey(base)) {
            return false;
        }
        this.base = base;
        this.conversionRate = currencyMap.get(base) / currencyMap.get("EUR");
        return true;
    }

    /**
     * Adds a currency to the properties file if it doesn't already exist and has valid values.
     * @param name name of the currency
     * @param rate rate of the currency
     * @return true if a new currency is added, false otherwise
     */
    public boolean addCurrency(String name, double rate) {
        if (name == null || rate <= 0 || currencyMap.containsKey(name)) return false;
        try (OutputStream outputstream = new FileOutputStream(path)) {
            Properties prop = new Properties();
            prop.setProperty(name, String.valueOf(rate));
            prop.store(outputstream, "new currency added");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
