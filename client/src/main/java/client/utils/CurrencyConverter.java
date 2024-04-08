package client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;

import java.io.*;
import java.net.ConnectException;
import java.util.*;

/**
 * Currency converter which can scan the currencies.properties file to
 * create a map of currencies and respective exchange rates and fetch
 * exchange rates from openExchangeRates API.
 * Uses singleton pattern.
 */
public class CurrencyConverter {

    private final File rates;
    private final Set<String> currencies;
    private final ServerUtils server;


    /**
     * @param server ServerUtils for testing
     */
    public CurrencyConverter(ServerUtils server, InputStream currencyStream, File rates) throws IOException {
        this.rates = rates;
        currencies = new ObjectMapper().reader().forType(Set.class).readValue(currencyStream);
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
        try {
            this.server = new ServerUtilsImpl(new UserConfig(
                    new FileIO(Objects.requireNonNull(CurrencyConverter.
                    class.getClassLoader().getResource("client/config.properties")))));
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
        // get last date from file
        List<String> temp;
        try {
            temp = new BufferedReader(new FileReader(path)).lines().toList();
        } catch (Exception e){ throw new RuntimeException();}
        String date = null;
        for(int i = 0; i < temp.size(); i++){
            if(temp.get(i).equals("#Last fetched:")){
                date = temp.get(i+1);
                break;
            }
        }
        if(temp.isEmpty()) {
            try {
                return server.getExchangeRates(null);
            } catch (ConnectException e) {
                //TODO should perhaps use the mainCtrl.handleServerNotFound() method
                throw new RuntimeException(e);
            }
        }
        assert date != null;
        String[] array = date.split(" ");
        int month = toMonth(array[1]);
        int day = Integer.parseInt(array[2]);
        String[] time = array[3].split(":");
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);
        int second = Integer.parseInt(time[2]);
        int year = Integer.parseInt(array[5]);

        Calendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
        //pass along last fetched file to serverUtils
        try {
            return server.getExchangeRates(calendar);
        } catch (ConnectException e) {
            //TODO should perhaps use the mainCtrl.handleServerNotFound() method
            throw new RuntimeException(e);
        }
    }

    /**
     * @param month string value representing a month
     * @return the int value of the month derived from string value
     */
    public static int toMonth(String month) {
        return switch (month) {
            case "Jan" -> Calendar.JANUARY;
            case "Feb" -> Calendar.FEBRUARY;
            case "Mar" -> Calendar.MARCH;
            case "Apr" -> Calendar.APRIL;
            case "May" -> Calendar.MAY;
            case "Jun" -> Calendar.JUNE;
            case "Jul" -> Calendar.JULY;
            case "Aug" -> Calendar.AUGUST;
            case "Sep" -> Calendar.SEPTEMBER;
            case "Oct" -> Calendar.OCTOBER;
            case "Nov" -> Calendar.NOVEMBER;
            case "Dec" -> Calendar.DECEMBER;
            //should be all cases, so default should give error
            default -> throw new RuntimeException();
        };
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
            prop.store(outputstream, "Last fetched:");

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
