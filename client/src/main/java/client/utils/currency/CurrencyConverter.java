package client.utils.currency;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Currency converter which can scan the currencies.txt file to
 * create a map of currencies and respective exchange rates and fetch
 * exchange rates from openExchangeRates API.
 * Uses singleton pattern.
 */
public class CurrencyConverter {

    private final FileManager fileManager;
    private final List<String> currencies;
    private final ServerUtils server;
    private final LanguageConf languageConf;

    /**
     * @param server ServerUtils for testing
     * @param fileManager file manager for managing historical rates
     * @param languageConf language config for errors
     */
    @Inject
    public CurrencyConverter(ServerUtils server,
                             FileManager fileManager,
                              LanguageConf languageConf) {
        this.fileManager = fileManager;
        this.currencies = fileManager.getAvailableCurrencies();
        this.server = server;
        this.languageConf = languageConf;
    }

    /**
     * @param from currency code to convert from
     * @param to currency to convert to
     * @param amount amount of from currency
     * @param time date at which to convert
     * @return converted amount
     * @throws CurrencyConversionException the method will throw its own error pop-up
     * @throws ConnectException if server is unavailable
     */
    public double convert(String from, String to, double amount, Instant time)
            throws CurrencyConversionException, ConnectException {
        try {
            String date = DateTimeFormatter.ISO_DATE
                    .format(time.atZone(ZoneOffset.UTC).toLocalDate());
            Map<String, Double> rates = fileManager.get(date);
            if(rates == null) {
                rates = server.getExchangeRates(date); // fetch rates from server
                if(rates.containsKey("status")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            languageConf.get("Currency.IOError"));
                    alert.setHeaderText(languageConf.get("unexpectedError"));
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    alert.showAndWait();
                    throw new CurrencyConversionException("Server error: " + rates.get("status"));
                }
                double eur = rates.get("EUR");
                rates.replaceAll((cur,rate) -> rate/eur); // convert USD based rates to EUR based
                fileManager.add(rates, date); // cache the rates
            }
            return amount * (rates.get(to.toUpperCase()) / rates.get(from.toUpperCase()));
        } catch (ConnectException e) {
            throw e;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("Currency.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new CurrencyConversionException("IO error");
        }
    }

    /**
     * @return get a set of
     */
    public List<String> getCurrencies() {
        return currencies;
    }

    /**
     * Custom exception for currency conversion errors
     */
    public static class CurrencyConversionException extends Exception {
        /**
         * @param message  message
         */
        public CurrencyConversionException(String message) {
            super(message);
        }
    }
}
