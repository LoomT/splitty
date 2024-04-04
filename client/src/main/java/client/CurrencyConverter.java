package client;

import client.utils.FileIO;
import client.utils.ServerUtils;
import client.utils.ServerUtilsImpl;
import client.utils.UserConfig;
import com.google.inject.Inject;

import java.io.*;
import java.util.*;

/**
 * Currency converter which can scan the currencies.properties file to
 * create a map of currencies and respective exchange rates and fetch
 * exchange rates from openExchangeRates API.
 * Uses singleton pattern.
 */
public class CurrencyConverter {

    private static CurrencyConverter currencyConverter;
    private static Map<String, Double> currencyMap;
    private String base;
    private double conversionRate;
    private String path;
    @Inject
    private ServerUtils server;


    /**
     * @param server ServerUtils for testing
     */
    private CurrencyConverter(String base, double conversionRate, String path, ServerUtils server) {
        this.base = base;
        this.conversionRate = conversionRate;
        this.path = path;
        this.server = server;
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
        try {
            this.server = new ServerUtilsImpl(new UserConfig(new FileIO(Objects.requireNonNull(CurrencyConverter.
                    class.getClassLoader().getResource("client/config.properties")).getPath())));
        } catch (Exception ignored){}
        try (Reader fileReader = new FileReader(path)) {
            currencyMap = initializeCurrencyMap(fileReader);
            this.conversionRate = 1;
        } catch (IOException e) {
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
     * @param base           base currency used in the local application
     * @param conversionRate value of base divided by value of Euro
     * @param path           path of config file
     * @param server         Server utils to be able to set server to test server utils
     * @return Instance of currency converter
     */
    public static CurrencyConverter createInstance(
            String base, double conversionRate, String path, ServerUtils server) {
        if (currencyConverter == null) {
            currencyConverter = new CurrencyConverter(base, conversionRate, path, server);
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

        for (int i = 2; i < temp.size(); i++) {
            if(temp.get(i).startsWith("#") || temp.get(i).startsWith("base=")) continue;
            String[] tempArr = temp.get(i).split("=");
            result.put(tempArr[0], Double.parseDouble(tempArr[1]));
        }
        currencyMap = result;
        return result;
    }

    /**
     * @return a String of the http response.
     */
    public String getExchange() {
        return server.getExchangeRates();
    }

    /**
     *
     * @return updates the currencies.properties file by fetching
     * up-to-date exchange data from the API.
     */
    public boolean updateExchange() {
        String response;
        try {
            response = getExchange();
        }catch (Exception e){return false;}
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
     * @param newBase change the base currency of the user
     * @return true if the base currency can be changed, false otherwise
     */
    public boolean setBase(String newBase) {
        String oldBase = this.base;
        if (base == null || !currencyMap.containsKey(base)) {
            return false;
        }
        this.base = newBase;
        this.conversionRate = currencyMap.get(newBase) / currencyMap.get(oldBase);
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
        try (OutputStream outputstream = new FileOutputStream(path, true)) {
            Properties prop = new Properties();
            prop.setProperty(name, String.valueOf(rate));
            prop.store(outputstream, "new currency added");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * @return the current base
     */
    public String getBase() {
        return base;
    }

    /**
     * @return the current conversion rate
     */
    public double getConversionRate() {
        return conversionRate;
    }

    /**
     * remove the currency converter, to replace dependency injected with mock and vise versa
     */
    public static void removeCC(){
        currencyConverter = null;
    }
}
