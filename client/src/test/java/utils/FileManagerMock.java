package utils;

import client.utils.currency.FileManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManagerMock implements FileManager {
    private final Map<String, Map<String, Double>> cache;
    private final List<String> currencies;

    /**
     * Construct mock file manager
     */
    public FileManagerMock() {
        cache = new HashMap<>();
        currencies = List.of("EUR", "GBP", "USD", "CHF", "JPY");
    }

    /**
     * Saves the rates
     *
     * @param rates exchange rates
     * @param date  date of them, yyyy-mm-dd format
     */
    @Override
    public void add(Map<String, Double> rates, String date) {
        if(cache.containsKey(date)) return;
        cache.put(date, rates);
    }

    /**
     * @param date date of rates, yyyy-mm-dd format
     * @return rates, null if not found
     */
    @Override
    public Map<String, Double> get(String date) {
        return cache.get(date);
    }

    /**
     * @return list of available currencies from currencies.txt
     */
    @Override
    public List<String> getAvailableCurrencies() {
        return currencies;
    }

    /**
     * @return all files
     */
    public Map<String, Map<String, Double>> getCache() {
        return cache;
    }
}
