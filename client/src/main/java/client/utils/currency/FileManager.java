package client.utils.currency;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileManager {

    /**
     * Saves the rates
     *
     * @param rates exchange rates
     * @param date date of them, yyyy-mm-dd format
     */
    void add(Map<String, Double> rates, String date) throws IOException;

    /**
     * @param date date of rates, yyyy-mm-dd format
     * @return rates, null if not found
     */
    Map<String, Double> get(String date) throws IOException;

    /**
     * @return list of available currencies from currencies.txt
     */
    List<String> getAvailableCurrencies();
}
