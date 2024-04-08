package utils;

import client.utils.currency.FileManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileManagerMock implements FileManager {
    private final Map<String, Map<String, Double>> cache;
    private final Set<String> currencies;

    /**
     * Construct mock file manager
     */
    public FileManagerMock() {
        cache = new HashMap<>();
        currencies = Set.of("EUR", "GBP", "USD", "CHF", "JPY");
    }

    /**
     * Saves the rates
     *
     * @param rates exchange rates
     * @param date  date of them, yyyy-mm-dd format
     */
    @Override
    public void add(Map<String, Double> rates, String date) throws IOException {
        if(!cache.containsKey(date)) return;
        cache.put(date, rates);
    }

    /**
     * @param date date of rates, yyyy-mm-dd format
     * @return rates, null if not found
     */
    @Override
    public Map<String, Double> get(String date) throws IOException {
        return cache.get(date);
    }

    /**
     * @return list of available currencies from currencies.txt
     */
    @Override
    public List<String> getAvailableCurrencies() {
        return List.of();
    }

    /**
     * @return all files
     */
    public Map<String, Map<String, Double>> getCache() {
        return cache;
    }
}
